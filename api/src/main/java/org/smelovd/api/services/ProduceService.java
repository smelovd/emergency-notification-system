package org.smelovd.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.Notification;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.factories.NotificationRequestFactory;
import org.smelovd.api.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProduceService {

    @Value("${base-file-path}")
    private String BASE_FILE_PATH;
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final NotificationRepository notificationRepository;
    private final NotificationRequestFactory notificationRequestFactory;

    public Mono<Void> asyncParseAndProduce(NotificationRequest request) {
        return Mono.fromRunnable(() -> this.produceFromFile(request, 0L).subscribe());
    }

    public Mono<Void> asyncProduce(String templateId, String message) {
        log.info("Send notification with id: {}", templateId);
        return notificationRequestFactory.create(templateId, message)
                .flatMap(request -> notificationRepository.findAllByTemplateId(templateId)
                        .map(notification -> notification.toBuilder().requestId(request.getId()).build())
                        .doOnNext(this::pushToQueue).then());
    }

    public Mono<Void> produceFromFile(NotificationRequest request, Long currentParsedLine) {
        log.info("File parsing with request id: " + request.getId());
        return readFileLines(request.getTemplateId())
                .skip(currentParsedLine)
                .map(line -> mapLineToNotification(line, request.getTemplateId()))
                .buffer(500)
                .concatMap(notifications -> {
                    log.info("inserting buffer");
                    return notificationRepository.insert(notifications);
                })
                .map(notification -> notification.toBuilder().requestId(request.getId()).build())
                .doOnNext(this::pushToQueue)
                .then(notificationRequestFactory.updateIsParsed(request.getTemplateId(), true));
    }

    private Flux<String> readFileLines(String requestId) {
        return Flux.using(
                () -> new BufferedReader(new InputStreamReader(new FileInputStream(BASE_FILE_PATH + requestId + ".csv"))),
                reader -> Flux.fromStream(reader.lines()),
                reader -> {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private Notification mapLineToNotification(String line, String templateId) {
        var records = line.split(",");

        return Notification.builder()
                .serviceUserId(records[0])
                .sender(records[1])
                .templateId(templateId)
                .build();
    }

    public void pushToQueue(Notification notification) {
        kafkaTemplate.send(notification.getSender(), notification);
        log.info("Notification produced with id: {}", notification.getId());
    }
}
