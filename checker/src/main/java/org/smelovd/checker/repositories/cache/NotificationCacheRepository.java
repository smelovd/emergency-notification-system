package org.smelovd.checker.repositories.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.repositories.NotificationRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationCacheRepository {

    private final NotificationRepository notificationRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Flux<String> findAllCompletedKeys(String requestId) {
        return redisTemplate.opsForHash().entries(requestId)
                .handle((entry, synchronousSink) -> {
                    if (entry.getValue().toString().equals("0") ||
                            entry.getValue().toString().equals("1")) {
                        synchronousSink.next(entry.getKey().toString());
                    }
                });
    }

    public Flux<Notification> findAllByRequestIdAndStatus(String requestId, String status) {
        return redisTemplate.opsForHash().entries(requestId)
                .<String>handle((entry, synchronousSink) -> {
                    if (entry.getValue().toString().equals(status)) {
                        synchronousSink.next(entry.getKey().toString());
                    }
                })
                .buffer(250)
                .flatMap(notificationRepository::findAllById);
    }

    public Mono<Boolean> removeUnusedStatuses(String requestId) {
        return redisTemplate.opsForHash().delete(requestId);
    }
}
