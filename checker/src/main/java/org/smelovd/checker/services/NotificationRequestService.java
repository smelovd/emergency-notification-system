package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.entities.NotificationTemplate;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.smelovd.checker.repositories.NotificationTemplateRepository;
import org.smelovd.checker.repositories.cache.NotificationCacheRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRequestService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final RecoveryService recoveryService;
    private final NotificationCacheRepository notificationCacheRepository;
    private final NotificationRequestRepository notificationRequestRepository;

    public Mono<NotificationTemplate> updateIsParsed(String templateId, boolean isParsed) {
        return notificationTemplateRepository.findById(templateId)
                .flatMap(template -> Mono.just(template.toBuilder().isParsed(isParsed).build()))
                .flatMap(notificationTemplateRepository::save);
    }

    public Mono<NotificationRequest> updateAlreadySentStatus(NotificationRequest request) {
        return Mono.zip(
                        notificationCacheRepository.findAllCompletedKeys(request.getId()).count(),
                        notificationTemplateRepository.findById(request.getId())
                )
                .flatMap(tuple -> {
                    long completedCount = tuple.getT1();
                    long absoluteCount = tuple.getT2().getNotificationCount();

                    if (areAllNotificationsSent(completedCount, absoluteCount)) {
                        log.info("All messages sent, completing notification request with id: {}", request.getId()); //TODO saving
                        return this.completeRequest(request);
                    }
                    return Mono.just(request);
                })
                .filter(request1 -> request1.getStatus().equals("CREATED"));
    }

    private boolean areAllNotificationsSent(long completedCount, long absoluteCount) {
        return completedCount == absoluteCount;
    }

    private Mono<NotificationRequest> completeRequest(NotificationRequest request) {
        return this.updateStatus(request, "DONE")
                .doOnNext(isOk -> recoveryService.prevCompletedCount.remove(request.getId()))
                .flatMap(savedRequest -> notificationCacheRepository.cleanStatuses(request.getId()))
                .thenReturn(request);
    }

    private Mono<NotificationRequest> updateStatus(NotificationRequest request, String status) {
        return notificationRequestRepository.save(request.toBuilder().completedAt(new Date()).status(status).build());
    }
}
