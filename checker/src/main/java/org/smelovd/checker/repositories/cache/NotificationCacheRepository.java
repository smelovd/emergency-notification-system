package org.smelovd.checker.repositories.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.repositories.NotificationRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationCacheRepository {

    private final NotificationRepository notificationRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Integer> getStatus(String requestId, String id) {
        return redisTemplate.opsForHash().get(requestId, id)
                .map(object -> (int) object);
    }

    public Flux<String> getAllKeys(String requestId) {
        return redisTemplate.opsForHash().keys(requestId)
                .map(object -> (String) object);
    }

    private Flux<Map.Entry<Object, Object>> getAllEntries(String requestId) {
        return redisTemplate.opsForHash().keys(requestId)
                .concatMap(key -> redisTemplate.opsForHash()
                        .entries(key.toString()));
    }

    public Flux<String> findAllCompletedKeys(String requestId) {
        return getAllEntries(requestId)
                .filter(entry -> Integer.valueOf(entry.getValue().toString()).equals(0) || Integer.valueOf(entry.getValue().toString()).equals(1))
                .map(entry -> entry.getKey().toString());
    }

    public Flux<Notification> findAllByRequestIdAndStatus(String requestId, String status) {
        return redisTemplate.opsForHash().entries(requestId)
                //TODO .collectMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString())
                .<String>handle((entry, synchronousSink) -> {
                    if (entry.getValue().toString().equals(status)) {
                        synchronousSink.next(entry.getKey().toString());
                    }
                })
                .buffer(250)
                .flatMap(notificationRepository::findAllById);
    }

    public Flux<Notification> findAllWithTimeout(NotificationRequest request) {
        if (isTimeout(request.getCreatedAt())) {
            return notificationRepository.findAllByRequestId(request.getId())
                    .concatMap(notification -> this.isExist(request.getId(), notification.getId())
                            .filter(isExist -> !isExist)
                            .map(isExist -> notification));
        }
        return Flux.empty();
    }

    private boolean isTimeout(Date createdAt) {
        return createdAt.after(new Date(System.currentTimeMillis() + 60000));
    }

    private Mono<Boolean> isExist(String requestId, String id) {
        return redisTemplate.opsForHash().hasKey(requestId, id);
    }

    public Mono<Boolean> removeNotificationStatuses(String requestId) {
        return redisTemplate.opsForHash().delete(requestId);
    }
}
