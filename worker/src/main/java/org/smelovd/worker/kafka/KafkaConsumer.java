package org.smelovd.worker.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.Notification;
import org.smelovd.worker.repositories.MessageRepository;
import org.smelovd.worker.repositories.NotificationIdAndStatusRepository;
import org.smelovd.worker.senders.TestSender;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final TestSender testSender;
    private final NotificationIdAndStatusRepository notificationIdAndStatusRepository;
    private final MessageRepository messageRepository;

    @KafkaListener(topics = "TEST", groupId = "1", batch = "true", concurrency = "4")
    public void testTopicListener(List<Notification> notifications) {
        log.info("take a new batch from TEST topic");
        Flux.fromIterable(notifications)
                //TODO .concatMap(cacheService::validateAlreadySentMessages)
                .parallel(4) //TODO optimize count threads
                .runOn(Schedulers.parallel())
                .concatMap(notification -> messageRepository.getMessageByTemplateId(notification.getTemplateId())
                .flatMap(message -> testSender.send(notification.getServiceUserId(), message)
                .flatMap(status -> {
                            log.info("notification sent with id: {}, status: {}", notification.getId(), status);
                            return notificationIdAndStatusRepository.save(notification.getRequestId(), notification.getId(), status);
                        })))
                .sequential()
                .subscribe();
    }
}
