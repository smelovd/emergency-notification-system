package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationRequestStatus;
import org.smelovd.checker.entities.NotificationStatus;
import org.smelovd.checker.repositories.NotificationRepository;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final RecoveryService recoveryService;
    private static final Map<String, Long> prevCompletedCount = new HashMap<>();

    @Scheduled(fixedRate = 30000)
    public void check() {
        log.info("start checking");
        notificationRequestRepository.findAllByStatus(NotificationRequestStatus.CREATED)
                .concatMap(request -> Mono.zip(
                                notificationRepository.findAllByRequestIdAndStatus(request.getId(), NotificationStatus.DONE).count(),
                                notificationRepository.findAllByRequestIdAndStatus(request.getId(), NotificationStatus.CLIENT_ERROR).count(),
                                notificationRepository.findAllByRequestId(request.getId()).count()
                        )
                        .flatMap(tuple -> {
                            long completedCount = tuple.getT1() + tuple.getT2();
                            long currentParsedCount = tuple.getT3();
                            long absoluteParseCount = request.getNotificationCount();

                            if (areAllMessagesSent(completedCount, absoluteParseCount)) {
                                prevCompletedCount.remove(request.getId());
                                log.info("all messages sent, changing status to DONE, request id: " + request.getId());
                                return notificationRequestRepository.save(request.toBuilder().status(NotificationRequestStatus.DONE).build());
                            }

                            if (isServerParseDown(currentParsedCount, absoluteParseCount, completedCount, prevCompletedCount.getOrDefault(request.getId(), -1L))) {
                                log.info("Server, that parsing file, is down, start async parsing file");
                                recoveryService.fileRecoveryRequestSend(request.getId(), currentParsedCount).subscribe();
                            }
                            prevCompletedCount.put(request.getId(), completedCount);
                            return Mono.just(request);
                        }))


                .concatMap(request -> Flux.merge(
                                        notificationRepository.findAllByRequestIdAndStatus(request.getId(), NotificationStatus.SERVER_ERROR),
                                        notificationRepository.findAllByRequestIdAndStatusAndLastUpdatedAtBefore(request.getId(), NotificationStatus.CREATED, new Date(System.currentTimeMillis() - 60000L))
                                )
                                .concatMap(notification -> notificationRepository.save(notification.toBuilder().lastUpdatedAt(new Date()).status(NotificationStatus.CREATED).build()))
                                .concatMap(notification -> Mono.fromRunnable(() -> kafkaTemplate.send(notification.getNotificationService(), notification)))
                )
                .subscribe();
    }

    private boolean isServerParseDown(long currentParsedCount, long absoluteParseCount, long completedCount, Long prevCompletedCount) {
        return currentParsedCount != absoluteParseCount && completedCount == prevCompletedCount;
    }

    private boolean areAllMessagesSent(long completedCount, long absoluteParseCount) {
        return completedCount == absoluteParseCount;
    }
}
