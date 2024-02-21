package org.smelovd.worker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.NotificationRequest;
import org.smelovd.worker.repositories.NotificationRequestRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<String> getMessageByRequestId(String id) {
        return redisTemplate.opsForValue().get(id)
                .switchIfEmpty(notificationRequestRepository.findById(id)
                        .map(NotificationRequest::getMessage)
                        .flatMap(message -> redisTemplate.opsForValue().set(id, message).thenReturn(message)));
    }
}
