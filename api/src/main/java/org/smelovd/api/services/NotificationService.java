package org.smelovd.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.Notification;
import org.smelovd.api.entities.NotificationStatus;
import org.smelovd.api.repositories.NotificationRepository;
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

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;

    public Mono<Void> produceNotifications(String notificationId) {
        log.info("File parsing with notification id: " + notificationId);
        return Flux.using(
                        () -> new BufferedReader(new InputStreamReader(new FileInputStream(notificationId + ".csv"))),
                        reader -> Flux.fromStream(reader.lines()),
                        reader -> {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .map(string -> {
                    var record = string.split(",");
                    return Notification.builder()
                            .fileId(record[0])
                            .serviceUserId(record[1])
                            .notificationService(record[2])
                            .requestId(notificationId)
                            .status(NotificationStatus.CREATED)
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
