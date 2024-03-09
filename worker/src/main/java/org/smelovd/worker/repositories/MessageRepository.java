package org.smelovd.worker.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.entities.NotificationTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MessageRepository {

    private final NotificationTemplateRepository notificationTemplateRepository;

    public Mono<String> getMessageByTemplateId(String templateId) {
        return notificationTemplateRepository.findById(templateId)
                .map(NotificationTemplate::getMessage)
                .cache(Duration.ofMinutes(15));
    }
}
