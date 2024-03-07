package org.smelovd.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.Notification;
import org.smelovd.api.repositories.NotificationRepository;
import org.smelovd.api.repositories.cache.NotificationCacheRepository;
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
public class NotificationService {

    @Value("${base-file-path}")
    private String BASE_FILE_PATH;
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final NotificationRequestService notificationRequestService;
    private final NotificationCacheRepository notificationCacheRepository;

    public Mono<Void> asyncParseAndProduce(String requestId) {
        return Mono.fromRunnable(() -> produceFromFile(requestId, 0L).subscribe());
    }

    public Mono<Void> asyncProduce(String requestId) {
        log.info("Send notification with id: {}", requestId);
        return notificationRepository.findAllByRequestId(requestId)
                .doOnNext(this::pushToQueue).then();
    }

    public Mono<Void> asyncRecoveryParseAndProduce(String requestId, String currentParsedCount) {
        return Mono.fromRunnable(() -> produceFromFile(requestId, Long.valueOf(currentParsedCount)).subscribe());
    }

    public Mono<Void> asyncRecoveryProduce(String requestId) {
        return Mono.fromRunnable(() -> notificationCacheRepository.findAllIdsByRequestId(requestId).collectList()
                .flatMapMany(producedNotifications -> notificationRepository.findAllByRequestId(requestId)
                            .filter(notification -> !producedNotifications.contains(notification.getId()))
                            .doOnNext(this::pushToQueue))
                .subscribe());
    }

    private Mono<Void> produceFromFile(String requestId, Long currentParsedLine) {
        log.info("File parsing with request id: " + requestId);
        return readFileLines(requestId)
                .skip(currentParsedLine)
                .map(line -> mapLineToNotification(line, requestId))
                .buffer(250)
                .concatMap(notifications -> {
                    log.info("inserting buffer");
                    return notificationRepository.insert(notifications);
                })
                .doOnNext(this::pushToQueue)
                .then(notificationRequestService.updateIsParsed(requestId, true));
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

    private Notification mapLineToNotification(String line, String requestId) {
        var records = line.split(",");

        return Notification.builder()
                .serviceUserId(records[0])
                .sender(records[1])
                .requestId(requestId)
                .build();
    }

    private void pushToQueue(Notification notification) {
        kafkaTemplate.send(notification.getSender(), notification);
        log.info("Notification produced with id: {}", notification.getId());
    }
}
