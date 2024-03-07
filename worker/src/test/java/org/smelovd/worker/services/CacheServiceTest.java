package org.smelovd.worker.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smelovd.worker.entities.Notification;
import org.smelovd.worker.repositories.cache.NotificationCacheRepository;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataRedisTest
@AutoConfigureWebTestClient
class CacheServiceTest {

    @InjectMocks
    private CacheService cacheService;
    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;
    @Mock
    private NotificationCacheRepository notificationCacheRepository;

    @BeforeEach
    void setUp() {
        ReactiveHashOperations hashOps = mock(ReactiveHashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void validateAlreadySentMessages_shouldReturnNotificationThatNotFoundInCache() {
        Notification notification = Notification.builder().id("1").requestId("1").build();

        when(notificationCacheRepository.getStatus(notification.getRequestId(), notification.getId())).thenReturn(Mono.empty());

        StepVerifier.create(cacheService.validateAlreadySentMessages(notification))
                .expectNext(notification)
                .verifyComplete();
    }

    @Test
    void validateAlreadySentMessages_shouldReturnMonoErrorBecauseNotificationStatusIsDone() {
        Notification notification = Notification.builder().id("1").requestId("1").build();

        when(notificationCacheRepository.getStatus(notification.getRequestId(), notification.getId())).thenReturn(Mono.just("0"));

        StepVerifier.create(cacheService.validateAlreadySentMessages(notification))
                .expectError()
                .verify();
    }

    @Test
    void validateAlreadySentMessages_shouldReturnMonoErrorBecauseNotificationStatusIsClientError() {
        Notification notification = Notification.builder().id("1").requestId("1").build();

        when(notificationCacheRepository.getStatus(notification.getRequestId(), notification.getId())).thenReturn(Mono.just("1"));

        StepVerifier.create(cacheService.validateAlreadySentMessages(notification))
                .expectError()
                .verify();
    }

    @Test
    void validateAlreadySentMessages_shouldReturnNotificationWithStatusServerError() {
        Notification notification = Notification.builder().id("1").requestId("1").build();

        when(notificationCacheRepository.getStatus(notification.getRequestId(), notification.getId())).thenReturn(Mono.just("2"));

        StepVerifier.create(cacheService.validateAlreadySentMessages(notification))
                .expectNext(notification)
                .verifyComplete();
    }
}