package org.smelovd.api.service;

import org.smelovd.api.entity.Notification;
import org.smelovd.api.entity.NotificationStatus;
import org.smelovd.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.sql.Timestamp;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;

    public Flux<Notification> getFluxNotifications(MultipartFile file, String id) throws IOException, InterruptedException {
        log.info("sending notification \"{}\"", file.getOriginalFilename());
        return Flux.fromStream(getRecords(file))
                .flatMap(r -> mapNotification(r, id));
    }

    private Stream<CSVRecord> getRecords(MultipartFile file) throws IOException {
        log.info("file parsing " + file.getOriginalFilename());
        Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            return CSVFormat.RFC4180.builder()
                    .setHeader("id", "service_user_id", "notification_service").build().parse(reader).stream();

    }

    private Mono<Notification> mapNotification(CSVRecord record, String notificationId) {
        log.info("mapping notification, " + record);
        String fileId = record.get("id");
        String serviceUserId = record.get("service_user_id");
        String service = record.get("notification_service");
        return Mono.just(Notification.builder()
                .fileId(fileId)
                .serviceUserId(serviceUserId)
                .notificationService(service)
                .notificationId(notificationId)
                .status(NotificationStatus.CREATED)
                .timestamp(new Timestamp(System.currentTimeMillis())).build());
    }
}
