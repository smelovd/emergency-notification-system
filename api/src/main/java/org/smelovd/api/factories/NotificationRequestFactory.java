package org.smelovd.api.factories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.repositories.NotificationRequestRepository;
import org.smelovd.api.repositories.NotificationTemplateRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRequestFactory {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationRequestRepository notificationRequestRepository;



    public Mono<Void> updateIsParsed(String requestId, boolean isParsed) {
        return notificationTemplateRepository.findById(requestId)
                .flatMap(request -> Mono.just(request.toBuilder().isParsed(isParsed).build()))
                .flatMap(notificationTemplateRepository::save).then();
    }

    public Mono<NotificationRequest> create(String templateId, String message) { //TODO message changing in template
        return notificationRequestRepository.insert(NotificationRequest.builder()
                .createdAt(new Date())
                .status("CREATED")
                .templateId(templateId)
                .build());
    }
}
