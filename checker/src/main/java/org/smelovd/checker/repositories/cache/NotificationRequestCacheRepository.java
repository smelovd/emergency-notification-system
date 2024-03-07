package org.smelovd.checker.repositories.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationRequestCacheRepository {

    private final NotificationRequestRepository notificationRequestRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final String REQUEST_STATUS_DICTIONARY = "request:status";


    public Flux<NotificationRequest> findAllByStatus(String status) {
        return redisTemplate.opsForHash().keys(REQUEST_STATUS_DICTIONARY)
                .concatMap(key -> redisTemplate.opsForHash().get(REQUEST_STATUS_DICTIONARY, key)
                        .filter(value -> value.equals(status))
                        .thenReturn(key))
                .concatMap(key -> notificationRequestRepository.findById(key.toString()));
    }

    public Mono<Boolean> updateStatus(String id, String status) {
        return redisTemplate.opsForHash().put(REQUEST_STATUS_DICTIONARY, id, status);
    }
}
