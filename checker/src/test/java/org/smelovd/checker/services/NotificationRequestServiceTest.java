package org.smelovd.checker.services;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.entities.NotificationTemplate;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.smelovd.checker.repositories.NotificationTemplateRepository;
import org.smelovd.checker.repositories.cache.NotificationCacheRepository;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DataRedisTest
@AutoConfigureWebTestClient
class NotificationRequestServiceTest {

    @InjectMocks
    private NotificationRequestService notificationRequestService;
    @Mock
    private NotificationCacheRepository notificationCacheRepository;
    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;
    @Mock
    private NotificationRequestRepository notificationRequestRepository;
    @Mock
    private RecoveryService recoveryService;

    @Test
    void updateAlreadySentStatus_shouldReturnRequest() {
        NotificationRequest request = NotificationRequest.builder()
                .id("1").status("CREATED").templateId("2").build();
        NotificationTemplate template = NotificationTemplate.builder()
                .id("2").isParsed(false).notificationCount(5L).message("message").build();

        when(notificationCacheRepository.findAllCompletedKeys(request.getId())).thenReturn(Flux.fromIterable(List.of(1, 2)).map(Object::toString));
        when(notificationTemplateRepository.findById(request.getTemplateId())).thenReturn(Mono.just(template));

        StepVerifier.create(notificationRequestService.updateAlreadySentStatus(request))
                .expectNext(request)
                .verifyComplete();
    }

    @Test
    void updateAlreadySentStatus_shouldCompleteRequest() {
        NotificationRequest request = NotificationRequest.builder()
                .id("1").status("CREATED").templateId("2").build();
        NotificationTemplate template = NotificationTemplate.builder()
                .id("2").isParsed(false).notificationCount(5L).message("message").build();

        when(notificationTemplateRepository.findById(request.getTemplateId())).thenReturn(Mono.just(template));
        when(notificationCacheRepository.findAllCompletedKeys(request.getId())).thenReturn(Flux.fromIterable(List.of(1, 2, 3, 4, 5)).map(Object::toString));

        //clearing
        when(notificationRequestRepository.save(any(NotificationRequest.class))).thenReturn(Mono.just(request));
        when(notificationCacheRepository.removeUnusedStatuses(request.getId())).thenReturn(Mono.just(true));

        StepVerifier.create(notificationRequestService.updateAlreadySentStatus(request))
                .expectNext(request)
                .verifyComplete();
    }
}