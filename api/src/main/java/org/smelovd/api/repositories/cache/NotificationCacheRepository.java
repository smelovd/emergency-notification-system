package org.smelovd.api.repositories.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCacheRepository {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Boolean> save(String requestId, String notificationId, int status) {
        return redisTemplate.opsForHash().put(requestId, notificationId, status)
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(2000)));
    }


    public Mono<Integer> getStatus(String requestId, String id) {
        return redisTemplate.opsForHash().get(requestId, id)
                .map(object -> (int) object);
    }

    public Flux<String> findAllIdsByRequestId(String requestId) {
        return redisTemplate.opsForHash().keys(requestId)
                .map(Object::toString);
    }

//    public boolean isShouldSend(Notification notification) {
//        return redisTemplate.opsForHash().hasKey(notification.getRequestId(), notification.getId())
//                .flatMap(isExist -> redisTemplate.opsForHash().get(notification.getRequestId(), notification.getId()))
//                .filter(status -> !status.equals(2)).thenReturn(notification);
//    }
}
