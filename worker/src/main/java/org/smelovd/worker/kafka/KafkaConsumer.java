package org.smelovd.worker.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.Notification;
import org.smelovd.worker.repositories.NotificationRepository;
import org.smelovd.worker.services.CacheService;
import org.smelovd.worker.services.senders.TestSenderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final NotificationRepository notificationRepository;
    private final TestSenderService testSenderService;
    private final CacheService cacheService;

    @KafkaListener(topics = "TEST", groupId = "1", batch = "true", concurrency = "8")
    public void notificationListener(List<Notification> notifications) {
        log.info("take a new batch from TEST topic");
        Flux.fromIterable(notifications)
                .flatMap(notification -> {
                    log.info("try to send notification with id: " + notification.getId());
                    return testSenderService.send(notification.getServiceUserId(), cacheService.getMessageByRequestId(notification.getRequestId()))
                    .map(status -> notification.toBuilder().status(status).build());
                })
                .flatMap(response -> {
            log.info("notification sent with id: " + response.getId() + " status: " + response.getStatus());
            return notificationRepository.save(response);
        })
                //.subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
