package org.smelovd.api.repositories.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCacheRepository {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Flux<String> findAllIdsByRequestId(String requestId) {
        return redisTemplate.opsForHash().keys(requestId)
                .map(Object::toString);
    }
}
