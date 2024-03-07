package org.smelovd.worker.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smelovd.worker.repositories.NotificationRequestRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

class KafkaConsumerTest {

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;
    @Mock
    private NotificationRequestRepository notificationRequestRepository;

    @BeforeEach
    void setUp() {
        //ReactiveHashOperations hashOps = mock(ReactiveHashOperations.class);
        //when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

//    @Test
//    void testTopicListener() {
//        List<Notification> notifications = {
//                Notification.builder().id().build(),
//                Notification.builder().id().build()
//        };
//        kafkaConsumer.testTopicListener(notifications);
//        StepVerifier.create(kafkaConsumer.testTopicListener(notifications))
//                .expectNext()
//    }
}