package org.smelovd.api.service;

import org.aspectj.lang.annotation.Aspect;
import org.smelovd.api.entity.Notification;
import org.smelovd.api.entity.NotificationRequest;
import org.smelovd.api.entity.NotificationStatus;
import org.smelovd.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.sql.Timestamp;
import java.util.stream.Stream;

@Slf4j
@Service
@Aspect
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;

    @Async
    public void pushNotifications(MultipartFile file, String id) {

        notificationRepository.insert(getFluxNotifications(file, id))
                .subscribe(n -> kafkaTemplate.send(n.getNotificationService(), n));
    }

    public Flux<Notification> getFluxNotifications(MultipartFile file, String id) {
        log.info("sending notification \"{}\"", file.getOriginalFilename());
        try {
            return Flux.fromStream(getRecords(file))
                    .concatMap(r -> mapNotification(r, id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<CSVRecord> getRecords(MultipartFile file) throws IOException {
        log.info("file parsing " + file.getOriginalFilename());
        Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            return CSVFormat.RFC4180.builder()
                    .setHeader("id", "service_user_id", "notification_service").build().parse(reader).stream();

    }

    private Mono<Notification> mapNotification(CSVRecord record, String notificationId) {
        log.info("mapping notification: " + record);
        return Mono.just(Notification.builder()
                .fileId(record.get("id"))
                .serviceUserId(record.get("service_user_id"))
                .notificationService(record.get("notification_service"))
                .notificationId(notificationId)
                .status(NotificationStatus.CREATED)
                .timestamp(new Timestamp(System.currentTimeMillis())).build());
    }


}
