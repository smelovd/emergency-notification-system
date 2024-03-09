package org.smelovd.worker.repositories.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smelovd.worker.entities.NotificationTemplate;
import org.smelovd.worker.repositories.MessageRepository;
import org.smelovd.worker.repositories.NotificationTemplateRepository;
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
class NotificationTemplateCacheRepositoryTest {

    @InjectMocks
    private MessageRepository messageRepository;
    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;
    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @BeforeEach
    void setUp() {
        ReactiveHashOperations hashOps = mock(ReactiveHashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void getMessage_shouldReturnMessageFromCache() {
        NotificationTemplate dbRequest = NotificationTemplate.builder().message("my-message-from-db").build();
        String requestId = "1";

        when(redisTemplate.opsForHash().get("request:message", requestId)).thenReturn(Mono.just("my-message-from-cache"));
        when(notificationTemplateRepository.findById(requestId)).thenReturn(Mono.just(dbRequest));
        when(redisTemplate.opsForHash().put("request:message", requestId, "my-message-from-db")).thenReturn(Mono.just(true));


        StepVerifier.create(messageRepository.getMessageByTemplateId(requestId))
                .expectNext("my-message-from-db")
                .verifyComplete();

    }

    @Test
    void getMessage_shouldReturnMessageFromDbAndSaveInCache() {
        NotificationTemplate dbRequest = NotificationTemplate.builder().message("my-message-from-db").build();
        String requestId = "1";

        when(redisTemplate.opsForHash().get("request:message", requestId)).thenReturn(Mono.empty());
        when(notificationTemplateRepository.findById(requestId)).thenReturn(Mono.just(dbRequest));
        when(redisTemplate.opsForHash().put("request:message", requestId, "my-message-from-db")).thenReturn(Mono.just(true));


        StepVerifier.create(messageRepository.getMessageByTemplateId(requestId))
                .expectNext("my-message-from-db")
                .verifyComplete();
    }
}