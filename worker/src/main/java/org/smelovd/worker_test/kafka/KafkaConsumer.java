package org.smelovd.worker_test.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker_test.entities.Notification;
import org.smelovd.worker_test.repositories.NotificationRepository;
import org.smelovd.worker_test.services.CacheService;
import org.smelovd.worker_test.services.TestSenderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final NotificationRepository notificationRepository;
    private final TestSenderService testSenderService;
    private final CacheService cacheService;

    @KafkaListener(topics = "TEST", groupId = "1", batch = "true", concurrency = "8")
    public Flux<Notification> notificationListener(List<Notification> notifications) {

        var fluxNotifications = Flux.fromIterable(notifications)
                .flatMap(notification -> testSenderService.send(notification.getServiceUserId(), cacheService.getMessageByRequestId(notification.getNotificationId()))
                        .map(status -> notification.toBuilder().status(status).build()));

        return notificationRepository.saveAll(fluxNotifications);
    }
}
