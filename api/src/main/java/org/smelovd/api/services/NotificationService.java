package org.smelovd.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.Notification;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.entities.NotificationStatus;
import org.smelovd.api.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${base-file-path}")
    private String BASE_FILE_PATH;
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;

    public Mono<Void> produce(String requestId, Long currentParsedLine) {
        log.info("File parsing with notification id: " + requestId);
        return readFileLines(requestId)
                .skip(currentParsedLine) //TODO maybe skip not parsed notifications
                .map(line -> mapLineToNotification(line, requestId))
                .concatMap(notificationRepository::insert)
                .concatMap(this::pushToQueue)
                .then();
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

    private Mono<Notification> pushToQueue(Notification notification) {
        return Mono.fromRunnable(() -> {
            log.info("Notification inserted: {}", notification);
            kafkaTemplate.send(notification.getNotificationService(), notification);
            log.info("Notification sent to Kafka: {}", notification);
        }).thenReturn(notification);
    }

    private Notification mapLineToNotification(String string, String requestId) {

        var record = string.split(",");

        return Notification.builder()
                .serviceUserId(record[0])
                .notificationService(record[1])
                .requestId(requestId)
                .status(NotificationStatus.CREATED)
                .createdAt(new Date())
                .lastUpdatedAt(new Date()).build();
    }

    public Mono<Object> asyncProduce(NotificationRequest request) {
        log.info("saved request " + request);
        return Mono.fromRunnable(() -> produce(request.getId(), 0L)
                .subscribeOn(Schedulers.boundedElastic()).subscribe());
    }

    public Mono<Object> recoveryAsyncProduce(String requestId, String currentParsedCount) {
        log.info("recovery notification with request id: {}", requestId);
        return Mono.fromRunnable(() -> produce(requestId, Long.valueOf(currentParsedCount))
                .subscribeOn(Schedulers.boundedElastic()).subscribe());
    }
}
