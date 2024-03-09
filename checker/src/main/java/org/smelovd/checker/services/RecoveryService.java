package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.entities.NotificationTemplate;
import org.smelovd.checker.repositories.NotificationRepository;
import org.smelovd.checker.repositories.NotificationTemplateRepository;
import org.smelovd.checker.repositories.cache.NotificationCacheRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryService {

    private final NotificationRepository notificationRepository;
    private final NotificationCacheRepository notificationCacheRepository;
    private final WebClientService webClientService;
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final NotificationTemplateRepository notificationTemplateRepository;
    protected final Map<String, Long> prevCompletedCount = new HashMap<>();

    public Mono<NotificationRequest> fixServerDown(NotificationRequest request) {
        return Mono.zip(
                notificationCacheRepository.findAllCompletedKeys(request.getId()).count(),
                notificationRepository.countAllByTemplateId(request.getTemplateId()),
                notificationTemplateRepository.findById(request.getTemplateId())
        ).flatMap(tuple -> {
            long completedCount = tuple.getT1();
            long currentParsedCount = tuple.getT2();
            NotificationTemplate template = tuple.getT3();
            log.info("completed count: {}, current parsed count: {}, request id: {}", completedCount, currentParsedCount, request.getId());

            if (isServerDown(request.getId(), completedCount)) {
                log.info("Server parsing file is down, initiating async recovery");
                log.info("request id: {}, is parsed: {}, parsed count: {}",request.getId(), template.isParsed(), currentParsedCount);
                return webClientService.sendRecovery(request.getId(), template.isParsed(), currentParsedCount)
                        .thenReturn(request);
            }
            prevCompletedCount.put(request.getId(), completedCount);
            return Mono.just(request);
        });
    }

    public Flux<Notification> produceServerErrorNotifications(NotificationRequest request) {
        return notificationCacheRepository.findAllByRequestIdAndStatus(request.getId(), "2")
                .doOnNext(notification -> {
                    log.info("Push notification with id: {}, to queue", notification.getId());
                    kafkaTemplate.send(notification.getSender(), notification.toBuilder().requestId(request.getId()).build());
                });
    }

    public void clearPrevCompletedCount(String requestId) {
        prevCompletedCount.remove(requestId);
    }

    private boolean isServerDown(String requestId, long completedCount) {
        return completedCount == prevCompletedCount.getOrDefault(requestId, -1L);
    }
}
