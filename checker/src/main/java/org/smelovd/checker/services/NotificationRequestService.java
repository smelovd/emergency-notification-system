package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.smelovd.checker.repositories.cache.NotificationCacheRepository;
import org.smelovd.checker.repositories.cache.NotificationRequestCacheRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRequestService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final NotificationRequestCacheRepository notificationRequestCacheRepository;
    private final RecoveryService recoveryService;
    private final NotificationCacheRepository notificationCacheRepository;

    public Mono<NotificationRequest> updateIsParsed(String requestId, boolean isParsed) {
        return notificationRequestRepository.findById(requestId)
                .flatMap(request -> Mono.just(request.toBuilder().isParsed(isParsed).build()))
                .flatMap(notificationRequestRepository::save);
    }

    public Mono<NotificationRequest> updateAlreadySentStatus(NotificationRequest request) {
        return notificationCacheRepository.findAllCompletedKeys(request.getId()).count()
                .flatMap(completedCount -> {
                    long absoluteCount = request.getNotificationCount();

                    if (areAllNotificationsSent(completedCount, absoluteCount)) {
                        log.info("All messages sent, completing notification request with id: {}", request.getId()); //TODO saving
                        return this.completeRequest(request);
                    }

                    return Mono.just(request);
                });
    }

    private boolean areAllNotificationsSent(long completedCount, long absoluteCount) {
        return completedCount == absoluteCount;
    }

    private Mono<NotificationRequest> completeRequest(NotificationRequest request) {
        return notificationRequestCacheRepository.updateStatus(request.getId(), "0")
                .doOnNext(isOk -> recoveryService.prevCompletedCount.remove(request.getId()))
                .flatMap(savedRequest -> notificationCacheRepository.removeNotificationStatuses(request.getId()))
                .thenReturn(request); //TODO ...
    }
}
