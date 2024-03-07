package org.smelovd.worker.repositories.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smelovd.worker.entities.Notification;
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
class NotificationCacheRepositoryTest {

    @InjectMocks
    private NotificationCacheRepository notificationCacheRepository;

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        ReactiveHashOperations hashOps = mock(ReactiveHashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void save_shouldReturnTrue() {
        Notification response = Notification.builder().requestId("1").id("1").build();
        String status = "0";

        when(redisTemplate.opsForHash().put(response.getRequestId(), response.getId(), status)).thenReturn(Mono.just(true));

        StepVerifier.create(notificationCacheRepository.save(response.getRequestId(), response.getId(), status))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void save_shouldReturnFalse() {
        Notification response = Notification.builder().requestId("1").id("1").build();
        String status = "0";

        when(redisTemplate.opsForHash().put(response.getRequestId(), response.getId(), status)).thenReturn(Mono.just(false));

        StepVerifier.create(notificationCacheRepository.save(response.getRequestId(), response.getId(), status))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void getStatus_shouldReturn0() {
        Notification response = Notification.builder().requestId("1").id("1").build();

        when(redisTemplate.opsForHash().get(response.getRequestId(), response.getId())).thenReturn(Mono.just(0));

        StepVerifier.create(notificationCacheRepository.getStatus(response.getRequestId(), response.getId()))
                .expectNext("0")
                .verifyComplete();
    }

    @Test
    void getStatus_shouldReturn1() {
        Notification response = Notification.builder().requestId("1").id("1").build();

        when(redisTemplate.opsForHash().get(response.getRequestId(), response.getId())).thenReturn(Mono.just(1));

        StepVerifier.create(notificationCacheRepository.getStatus(response.getRequestId(), response.getId()))
                .expectNext("1")
                .verifyComplete();
    }

    @Test
    void getStatus_shouldReturn2() {
        Notification response = Notification.builder().requestId("1").id("1").build();

        when(redisTemplate.opsForHash().get(response.getRequestId(), response.getId())).thenReturn(Mono.just(2));

        StepVerifier.create(notificationCacheRepository.getStatus(response.getRequestId(), response.getId()))
                .expectNext("2")
                .verifyComplete();
    }
}