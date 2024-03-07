package org.smelovd.api.repositories.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.repositories.NotificationRequestRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRequestCacheRepository {

    private final NotificationRequestRepository notificationRequestRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final String REQUEST_MESSAGE_DICTIONARY = "request:message";

    public Mono<String> getMessage(String requestId) {
        return redisTemplate.opsForHash()
                .get(REQUEST_MESSAGE_DICTIONARY, requestId)
                .map(Object::toString)
                .switchIfEmpty(notificationRequestRepository
                        .findById(requestId)
                        .map(NotificationRequest::getMessage)
                        .flatMap(message -> redisTemplate.opsForHash()
                                .put("messages", requestId, message)
                                .thenReturn(message)));
    }
}
