package org.smelovd.checker.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.entities.NotificationTemplate;
import org.smelovd.checker.repositories.NotificationRepository;
import org.smelovd.checker.repositories.NotificationTemplateRepository;
import org.smelovd.checker.repositories.cache.NotificationCacheRepository;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataRedisTest
@AutoConfigureWebTestClient
class RecoveryServiceTest {

    @InjectMocks
    private RecoveryService recoveryService;
    @Mock
    private NotificationCacheRepository notificationCacheRepository;
    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;
    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private WebClientService webClientService;
    @Mock
    private KafkaTemplate<String, Notification> kafkaTemplate;

    @BeforeEach
    void setUp() {
        ReactiveHashOperations hashOps = mock(ReactiveHashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void fixServerDown() {
    }

    @Test
    void produceServerErrorNotifications_shouldPushToKafka() {
        NotificationRequest request = NotificationRequest.builder()
                .id("1").status("CREATED").templateId("10").build();
        List<Notification> build = List.of(
                Notification.builder().id("1").sender("TEST").serviceUserId("1").templateId("10").build(),
                Notification.builder().id("2").sender("TEST").serviceUserId("2").templateId("10").build()
                );

        NotificationTemplate template = NotificationTemplate.builder()
                .id("2").isParsed(false).notificationCount(10L).message("message").build();

        when(notificationCacheRepository.findAllByRequestIdAndStatus(request.getId(), "2")).thenReturn(Flux.fromIterable(build));
        when(kafkaTemplate.send(anyString(), any(Notification.class))).thenReturn(CompletableFuture.completedFuture(null));

        StepVerifier.create(recoveryService.produceServerErrorNotifications(request))
                .expectNextCount(2)
                .verifyComplete();

        verify(kafkaTemplate, times(2)).send(anyString(), any(Notification.class));
    }

    @Test
    void clearPrevCompletedCount() {
    }

    @Test
    void fixServerDown_shouldReturnRequest() {
        NotificationRequest request = NotificationRequest.builder()
                .id("1").status("CREATED").templateId("2").build();
        NotificationTemplate template = NotificationTemplate.builder()
                .id("2").isParsed(false).notificationCount(5L).message("message").build();

        when(notificationCacheRepository.findAllCompletedKeys(request.getId())).thenReturn(Flux.fromIterable(List.of(1, 2)).map(Object::toString));
        when(notificationTemplateRepository.findById(request.getTemplateId())).thenReturn(Mono.just(template));
        when(notificationRepository.countAllByTemplateId(request.getTemplateId())).thenReturn(Mono.just(5L));

        StepVerifier.create(recoveryService.fixServerDown(request))
                .expectNext(request)
                .verifyComplete();
    }

    @Test
    void fixServerDown_shouldReturnRequestAndSendRecoveryRequest() {
        NotificationRequest request = NotificationRequest.builder()
                .id("1").status("CREATED").templateId("2").build();
        NotificationTemplate template = NotificationTemplate.builder()
                .id("2").isParsed(false).notificationCount(10L).message("message").build();

        recoveryService.prevCompletedCount.put(request.getId(), 2L);

        when(notificationCacheRepository.findAllCompletedKeys(request.getId())).thenReturn(Flux.fromIterable(List.of(1, 2)).map(Object::toString));
        when(notificationRepository.countAllByTemplateId(request.getTemplateId())).thenReturn(Mono.just(5L));
        when(notificationTemplateRepository.findById(request.getTemplateId())).thenReturn(Mono.just(template));

        when(webClientService.sendRecovery(request.getId(), false, 5L)).thenReturn(Mono.empty());

        StepVerifier.create(recoveryService.fixServerDown(request))
                .expectNext(request)
                .verifyComplete();

        verify(webClientService).sendRecovery(request.getId(), false, 5L);
    }
}