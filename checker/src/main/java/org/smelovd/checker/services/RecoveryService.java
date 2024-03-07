package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.repositories.NotificationRepository;
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
    private final ApiService apiService;
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    protected final Map<String, Long> prevCompletedCount = new HashMap<>();

    public Mono<NotificationRequest> fixServerDown(NotificationRequest request) {
        return Mono.zip(
                notificationCacheRepository.findAllCompletedKeys(request.getId()).count(),
                notificationRepository.countAllByRequestId(request.getId())
        ).flatMap(counts -> {
            long completedCount = counts.getT1();
            long currentParsedCount = counts.getT2();

            if (isServerDown(request.getId(), completedCount)) {
                log.info("Server parsing file is down, initiating async recovery");
                return apiService.sendRecovery(request, currentParsedCount);
            }

            prevCompletedCount.put(request.getId(), completedCount);
            return Mono.just(request);
        });
    }

    public Flux<Notification> produceServerErrorNotifications(NotificationRequest request) {
        return notificationCacheRepository.findAllByRequestIdAndStatus(request.getId(), "2")
                .doOnNext(notification -> {
                    log.info("Push notification with id: {}, to queue", notification.getId());
                    kafkaTemplate.send(notification.getSender(), notification);
                });
    }

    private boolean isServerDown(String requestId, long completedCount) {
        return completedCount == prevCompletedCount.getOrDefault(requestId, -1L);
    }
}
