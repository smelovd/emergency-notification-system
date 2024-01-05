package org.smelovd.worker_test.kafka;

import org.smelovd.worker_test.entity.Notification;
import org.smelovd.worker_test.repositories.NotificationRepository;
import org.smelovd.worker_test.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @KafkaListener(topics = "notifications", groupId = "1", batch = "true", concurrency = "4")
    public void notificationListener(List<Notification> notifications) {
        var updatedNotifications = Flux.fromIterable(notifications)
                .doOnNext(n -> n.setStatus(notificationService.send(n)))
                .publishOn(Schedulers.boundedElastic());
        notificationRepository.saveAll(updatedNotifications)
                .subscribe();
    }
}
