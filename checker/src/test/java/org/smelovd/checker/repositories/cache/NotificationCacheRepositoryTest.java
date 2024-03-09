package org.smelovd.checker.repositories.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.repositories.NotificationRepository;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataRedisTest
@AutoConfigureWebTestClient
class NotificationCacheRepositoryTest {

    @InjectMocks
    private NotificationCacheRepository notificationCacheRepository;
    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;
    @Mock
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        ReactiveHashOperations hashOps = mock(ReactiveHashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void findAllCompletedKeys() {
        Map<Object, Object> notificationIdStatus = new HashMap<>();
        notificationIdStatus.put("1", "0");
        notificationIdStatus.put("2", "1");
        notificationIdStatus.put("3", "1");
        notificationIdStatus.put("4", "2");
        notificationIdStatus.put("5", "2");
        notificationIdStatus.put("6", "2");

        NotificationRequest request = NotificationRequest.builder()
                .id("1").templateId("2").createdAt(new Date()).status("CREATED").build();

        when(redisTemplate.opsForHash().entries(request.getId())).thenReturn(Flux.fromIterable(notificationIdStatus.entrySet()));

        StepVerifier.create(notificationCacheRepository.findAllCompletedKeys(request.getId()))
                .expectNext("1", "2", "3")
                .verifyComplete();
    }

    @Test
    void findAllByTemplateIdAndStatus() {
        Map<Object, Object> notificationIdStatus = new HashMap<>();
        notificationIdStatus.put("1", "0");
        notificationIdStatus.put("2", "1");
        notificationIdStatus.put("3", "1");
        notificationIdStatus.put("4", "2");
        notificationIdStatus.put("5", "2");
        notificationIdStatus.put("6", "2");

        List<Notification> notifications = List.of(
                Notification.builder().id("1").templateId("10").serviceUserId("1").sender("TEST").requestId("20").build(),
                Notification.builder().id("2").templateId("10").serviceUserId("2").sender("TEST").requestId("20").build(),
                Notification.builder().id("3").templateId("11").serviceUserId("3").sender("TEST").requestId("21").build()
        );

        NotificationRequest request = NotificationRequest.builder()
                .id("1").templateId("2").createdAt(new Date()).status("CREATED").build();

        when(redisTemplate.opsForHash().entries(request.getId())).thenReturn(Flux.fromIterable(notificationIdStatus.entrySet()));
        when(notificationRepository.findAllById(any(List.class))).thenReturn(Flux.fromIterable(notifications));

        StepVerifier.create(notificationCacheRepository.findAllByRequestIdAndStatus(request.getId(), "2"))
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void getAllKeys() {
    }



    @Test
    void findAllWithTimeout() {
    }

    @Test
    void cleanStatuses() {
    }
}