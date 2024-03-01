package org.smelovd.worker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.Notification;
import org.smelovd.worker.repositories.NotificationRepository;
import org.smelovd.worker.services.senders.TestSenderService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static org.smelovd.worker.entities.NotificationStatus.CREATED;
import static org.smelovd.worker.entities.NotificationStatus.SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenderService {

    private final CacheService cacheService;
    private final TestSenderService testSenderService;
    private final NotificationRepository notificationRepository;

    public Mono<Notification> send(Notification notification) {
        log.info("try to send notification with id: " + notification.getId());
        return cacheService.getMessageFromCache(notification.getRequestId())
                .flatMap(message -> testSenderService.send(notification.getServiceUserId(), message)
                            .map(status -> notification.toBuilder().status(status).build()))
                .flatMap(response -> {
                    log.info("notification sent with id: " + response.getId() + " status: " + response.getStatus());
                    return notificationRepository.save(response);
                });
    }

    public Mono<Notification> validateSentMessages(Notification notification) {
        if (notification.getLastUpdatedAt().equals(notification.getCreatedAt())) {
            return Mono.just(notification);
        } else {
            return notificationRepository.findById(notification.getId())
                    .filter(savedNotification -> savedNotification.getStatus().equals(CREATED) || savedNotification.getStatus().equals(SERVER_ERROR));
        }
    }
}
