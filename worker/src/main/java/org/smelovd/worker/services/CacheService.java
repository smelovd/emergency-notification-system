package org.smelovd.worker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.Notification;
import org.smelovd.worker.repositories.cache.NotificationCacheRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final NotificationCacheRepository notificationCacheRepository;

    public Mono<Notification> validateAlreadySentMessages(Notification notification) {
        return notificationCacheRepository.getStatus(notification.getRequestId(), notification.getId())
                .<Notification>handle((status, synchronousSink) -> {
                    if (status.equals("2")) {
                        synchronousSink.next(notification);
                    } else {
                        synchronousSink.error(new RuntimeException());
                    }
                })
                .switchIfEmpty(Mono.just(notification));
    }
}
