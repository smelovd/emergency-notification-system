package org.smelovd.worker.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.Notification;
import org.smelovd.worker.services.SenderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final SenderService senderService;

    @KafkaListener(topics = "TEST", groupId = "1", batch = "true", concurrency = "8")
    public void notificationListener(List<Notification> notifications) {
        log.info("take a new batch from TEST topic");
        Flux.fromIterable(notifications)
                //.concatMap(senderService::validateSentMessages)
                .concatMap(senderService::send)
                .subscribe();
    }
}
