package org.smelovd.worker.repositories.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationCacheRepository {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Boolean> save(String requestId, String notificationId, String status) {
        return redisTemplate.opsForHash().put(requestId, notificationId, status);
    }


    public Mono<String> getStatus(String requestId, String id) {
        return redisTemplate.opsForHash().get(requestId, id)
                .map(Object::toString);
    }
}
