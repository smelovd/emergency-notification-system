package org.smelovd.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.smelovd.api.entities.Notification;
import org.smelovd.api.entities.NotificationStatus;
import org.smelovd.api.repositories.NotificationRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Timestamp;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;

    public Mono<Void> pushToDatabaseAndQueue(MultipartFile file, String notificationId) {
        log.info("file parsing with notification id: " + notificationId);
        return Mono.fromCallable(() -> {
                    try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                        var csvRecords = CSVFormat.RFC4180.builder()
                                .setHeader("id", "service_user_id", "notification_service")
                                .build().parse(reader);
                        return Flux.fromIterable(csvRecords)
                                .map(record ->
                                        Notification.builder()
                                                .fileId(record.get("id"))
                                                .serviceUserId(record.get("service_user_id"))
                                                .notificationService(record.get("notification_service"))
                                                .notificationId(notificationId)
                                                .status(NotificationStatus.CREATED)
                                                .timestamp(new Timestamp(System.currentTimeMillis())).build());
                    } catch (IOException e) {
                        log.error("file parsing error with notification id: " + notificationId);
                        throw new RuntimeException(e);
                    }
                })
                .flatMapMany(notificationRepository::insert)
                .concatMap(notification -> Mono.fromRunnable(() -> kafkaTemplate.send(notification.getNotificationService(), notification)))
                .then();
    }
}
