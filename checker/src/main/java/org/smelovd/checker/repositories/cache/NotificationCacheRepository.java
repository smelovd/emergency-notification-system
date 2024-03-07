package org.smelovd.checker.repositories.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationTemplate;
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

    public Mono<Integer> getStatus(String templateId, String id) {
        return redisTemplate.opsForHash().get(templateId, id)
                .map(object -> (int) object);
    }

    public Flux<String> getAllKeys(String templateId) {
        return redisTemplate.opsForHash().keys(templateId)
                .map(object -> (String) object);
    }

    private Flux<Map.Entry<Object, Object>> getAllEntries(String templateId) {
        return redisTemplate.opsForHash().keys(templateId)
                .concatMap(key -> redisTemplate.opsForHash()
                        .entries(key.toString()));
    }

    public Flux<String> findAllCompletedKeys(String templateId) {
        return getAllEntries(templateId)
                .filter(entry -> Integer.valueOf(entry.getValue().toString()).equals(0) || Integer.valueOf(entry.getValue().toString()).equals(1))
                .map(entry -> entry.getKey().toString());
    }

    public Flux<Notification> findAllByTemplateIdAndStatus(String templateId, String status) {
        return redisTemplate.opsForHash().entries(templateId)
                //TODO .collectMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString())
                .<String>handle((entry, synchronousSink) -> {
                    if (entry.getValue().toString().equals(status)) {
                        synchronousSink.next(entry.getKey().toString());
                    }
                })
                .buffer(250)
                .flatMap(notificationRepository::findAllById);
    }

    public Flux<Notification> findAllWithTimeout(NotificationTemplate template) {
        if (isTimeout(template.getCreatedAt())) {
            return notificationRepository.findAllByTemplateId(template.getId())
                    .concatMap(notification -> this.isExist(template.getId(), notification.getId())
                            .filter(isExist -> !isExist)
                            .map(isExist -> notification));
        }
        return Flux.empty();
    }

    private boolean isTimeout(Date createdAt) {
        return createdAt.after(new Date(System.currentTimeMillis() + 60000));
    }

    private Mono<Boolean> isExist(String templateId, String id) {
        return redisTemplate.opsForHash().hasKey(templateId, id);
    }

    public Mono<Boolean> cleanStatuses(String requestId) {
        return redisTemplate.opsForHash().delete(requestId);
    }
}
