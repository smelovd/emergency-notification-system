package org.smelovd.api.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smelovd.api.repositories.NotificationRequestRepository;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;

@DataRedisTest
@AutoConfigureWebTestClient
class NotificationRequestFactoryTest {

    @InjectMocks
    private NotificationRequestFactory notificationRequestFactory;
    @Mock
    private NotificationRequestRepository notificationRequestRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    void updateIsParsed() {
    }

    /*@Test
    void create() {
        NotificationTemplate template = NotificationTemplate.builder().id("1").message("message").build();
        NotificationRequest request = NotificationRequest.builder()
                .createdAt(new Date())
                .status("CREATED")
                .templateId(template.getId())
                .build();

        when(notificationRequestRepository.insert(any(NotificationRequest.class))).thenReturn(request);

        StepVerifier.create(notificationRequestFactory.create(template.getId(), template.getMessage()))
                .verifyComplete();

        verify(notificationRequestRepository).insert(any(NotificationRequest.class));
    }*/
}