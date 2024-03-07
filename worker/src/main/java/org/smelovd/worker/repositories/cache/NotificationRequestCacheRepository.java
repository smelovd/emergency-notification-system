package org.smelovd.worker.repositories.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.NotificationRequest;
import org.smelovd.worker.repositories.NotificationRequestRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationRequestCacheRepository {

    private final NotificationRequestRepository notificationRequestRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final String REQUEST_MESSAGE_DICTIONARY = "request:message";

    public Mono<String> getMessage(String requestId) {
        return notificationRequestRepository.findById(requestId)
                        .map(NotificationRequest::getMessage)
                .cache(Duration.ofMinutes(10));
    }
}
