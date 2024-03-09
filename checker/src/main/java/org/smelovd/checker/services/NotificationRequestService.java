package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.NotificationRequest;
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

    public Mono<NotificationRequest> updateAlreadySentStatus(NotificationRequest request) {
        return Mono.zip(
                        notificationCacheRepository.findAllCompletedKeys(request.getId()).count(),
                        notificationTemplateRepository.findById(request.getTemplateId()))
                .flatMap(tuple -> {
                    long completedCount = tuple.getT1();
                    long absoluteCount = tuple.getT2().getNotificationCount();
                    log.info("completed count: {}, absolute count: {}, request id: {}", completedCount, absoluteCount, request.getId());

                    if (areAllNotificationsSent(completedCount, absoluteCount)) {
                        log.info("All messages sent, completing notification request with id: {}", request.getId());
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
        return notificationRequestRepository.save(request.toBuilder().completedAt(new Date()).status("DONE").build())
                .doOnNext(isOk -> recoveryService.clearPrevCompletedCount(request.getId()))
                .flatMap(savedRequest -> notificationCacheRepository.removeUnusedStatuses(request.getId()))
                .thenReturn(request);
    }
}
