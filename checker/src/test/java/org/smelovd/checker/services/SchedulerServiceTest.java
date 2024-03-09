package org.smelovd.checker.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataRedisTest
@AutoConfigureWebTestClient
class SchedulerServiceTest {

    @InjectMocks
    private SchedulerService schedulerService;

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;
    @Mock
    private NotificationRequestRepository notificationRequestRepository;
    @Mock
    private NotificationRequestService notificationRequestService;
    @Mock
    private RecoveryService recoveryService;


    @BeforeEach
    void setUp() {
        ReactiveHashOperations hashOps = mock(ReactiveHashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void recoveryStarter() {
    }

    @Test
    void recovery_shouldReturn() {
        Notification notification = Notification.builder().build();
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .id("1")
                .status("CREATED")
                .createdAt(new Date())
                .templateId("1")
                .build();

        when(notificationRequestRepository.findAllByStatus("CREATED")).thenReturn(Flux.just(notificationRequest));
        when(notificationRequestService.updateAlreadySentStatus(notificationRequest)).thenReturn(Mono.just(notificationRequest));
        when(recoveryService.fixServerDown(notificationRequest)).thenReturn(Mono.just(notificationRequest));
        when(recoveryService.produceServerErrorNotifications(notificationRequest)).thenReturn(Flux.just(notification));

        StepVerifier.create(schedulerService.recovery())
                .expectNext(notification)
                .verifyComplete();
    }
}