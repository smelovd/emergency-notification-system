package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.entities.NotificationStatus;
import org.smelovd.checker.repositories.NotificationRepository;
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
public class RecoveryService {

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    @Value("${base-file-path}")
    private String BASE_FILE_PATH;

    public Mono<Void> recoveryProduce(NotificationRequest request, long currentParsedCount) {
        log.info("File recovery parsing with notification id: " + request.getId());
        return Flux.using(
                        () -> new BufferedReader(new InputStreamReader(new FileInputStream(BASE_FILE_PATH + request.getId() + ".csv"))),
                        reader -> Flux.fromStream(reader.lines()),
                        reader -> {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .skip(currentParsedCount) // TODO may save not sync, for example n1, n2, n4, then server down, and here skip count(n1,n2,n4) = 3, and push n4 again, skipped n3
                .map(line -> {
                    var record = line.split(",");
                    return Notification.builder()
                            .serviceUserId(record[0])
                            .notificationService(record[1])
                            .requestId(request.getId())
                            .status(NotificationStatus.CREATED)
                            .createdAt(new Date())
                            .lastUpdatedAt(new Date()).build();
                })
                .concatMap(notificationRepository::insert)
                .flatMap(insertedNotification -> Mono.fromRunnable(() -> {
                    log.info("Notification inserted: {}", insertedNotification);
                    kafkaTemplate.send(insertedNotification.getNotificationService(), insertedNotification);
                    log.info("Notification sent to Kafka: {}", insertedNotification);
                })).then();
    }
}
