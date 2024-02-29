package org.smelovd.worker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.NotificationRequest;
import org.smelovd.worker.repositories.NotificationRequestRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.WeakHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final WeakHashMap<String, String> messages = new WeakHashMap<>();

    public String getMessage(String serviceUserId) {
        if (messages.containsKey(serviceUserId)) {
            return messages.get(serviceUserId);
        }

        String message = getMessageFromCache(serviceUserId).blockOptional().orElseThrow();
        messages.put(serviceUserId, message);

        return message;
    }

    private Mono<String> getMessageFromCache(String id) { // no sense to use reactive caching, maybe in future it is
        return redisTemplate.opsForValue().get(id)
                .switchIfEmpty(notificationRequestRepository.findById(id)
                        .map(NotificationRequest::getMessage)
                        .flatMap(message -> redisTemplate.opsForValue().set(id, message).thenReturn(message)));
    }


}
