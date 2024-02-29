package org.smelovd.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.Notification;
import org.smelovd.api.entities.NotificationStatus;
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
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${base-file-path}")
    private String BASE_FILE_PATH;
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;

    public Mono<Void> produce(String notificationId) {
        return produce(notificationId, 0L);
    }

    public Mono<Void> produce(String notificationId, Long currentParsedCount) {
        log.info("File parsing with notification id: " + notificationId);
        return Flux.using(
                        () -> new BufferedReader(new InputStreamReader(new FileInputStream(BASE_FILE_PATH + notificationId + ".csv"))),
                        reader -> Flux.fromStream(reader.lines()),
                        reader -> {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .skip(currentParsedCount)
                .map(string -> {
                    var record = string.split(",");
                    return Notification.builder()
                            .serviceUserId(record[0])
                            .notificationService(record[1])
                            .requestId(notificationId)
                            .status(NotificationStatus.CREATED)
                            .createdAt(new Date())
                            .lastUpdatedAt(new Date()).build();
                })
                .concatMap(notificationRepository::insert)
                .concatMap(notification -> Mono.fromRunnable(() -> {
                    log.info("Notification inserted: {}", notification);
                    kafkaTemplate.send(notification.getNotificationService(), notification);
                    log.info("Notification sent to Kafka: {}", notification);
                }))
                .then();
    }
}
