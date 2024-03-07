package org.smelovd.worker.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.Notification;
import org.smelovd.worker.entities.NotificationRequest;
import org.smelovd.worker.repositories.NotificationRequestRepository;
import org.smelovd.worker.repositories.cache.NotificationCacheRepository;
import org.smelovd.worker.services.senders.TestSenderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final TestSenderService testSenderService;
    private final NotificationCacheRepository notificationCacheRepository;
    private final NotificationRequestRepository notificationRequestRepository;

    @KafkaListener(topics = "TEST", groupId = "1", batch = "true", concurrency = "4")
    public void testTopicListener(List<Notification> notifications) {
        log.info("take a new batch from TEST topic");
        Flux.fromIterable(notifications)
                //.concatMap(cacheService::validateAlreadySentMessages)
                .parallel(4)
                .runOn(Schedulers.parallel())
                .concatMap(notification -> notificationRequestRepository.findById(notification.getRequestId())
                        .map(NotificationRequest::getMessage)
                .flatMap(message -> testSenderService.send(notification.getServiceUserId(), message)
                        .flatMap(status -> {
                            log.info("notification sent with id: {}, status: {}", notification.getId(), status);
                            return notificationCacheRepository.save(notification.getRequestId(), notification.getId(), status);
                        })))
                .sequential()
                .subscribe();
    }
}
