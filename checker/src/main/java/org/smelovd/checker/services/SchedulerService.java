package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.NotificationRequestStatus;
import org.smelovd.checker.entities.NotificationStatus;
import org.smelovd.checker.repositories.NotificationRepository;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.smelovd.checker.services.exceptions.ServerFileParserDropException;
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
    private static final Map<String, Long> prevCompletedCount = new HashMap<>();

    @Scheduled(fixedRate = 20000)
    public void check() {
        log.info("start checking ");
        notificationRequestRepository.findAllByStatus(NotificationRequestStatus.CREATED)
                .concatMap(request -> Mono.zip(
                                notificationRepository.findAllByNotificationIdAndStatus(request.getId(), NotificationStatus.DONE).count(),
                                notificationRepository.findAllByNotificationIdAndStatus(request.getId(), NotificationStatus.CLIENT_ERROR).count(),
                                notificationRepository.findAllByNotificationId(request.getId()).count()
                        )
                        .doOnNext(System.out::println)
                        .flatMap(tuple -> {
                            long completedCount = tuple.getT1() + tuple.getT2();
                            long currentParsedCount = tuple.getT3();
                            long absoluteParseCount = request.getNotificationCount();

                            if (areAllNotificationsSent(completedCount, absoluteParseCount)) {
                                prevCompletedCount.remove(request.getId());
                                return notificationRequestRepository.save(request.toBuilder().status(NotificationRequestStatus.DONE).build());
                            }

                            if (isServerParseFileDrop(currentParsedCount, absoluteParseCount, completedCount, prevCompletedCount.getOrDefault(request.getId(), -1L))) {
                                return Mono.error(new ServerFileParserDropException());
                            }
                            prevCompletedCount.put(request.getId(), completedCount);
                            return Mono.just(request);
                        }))
                //.doOnError() //TODO

                .concatMap(request -> Flux.merge(
                                        notificationRepository.findAllByNotificationIdAndStatus(request.getId(), NotificationStatus.SERVER_ERROR),
                                        notificationRepository.findAllByNotificationIdAndStatusAndTimestampBefore(request.getId(), NotificationStatus.CREATED, new Date(System.currentTimeMillis() - 60000L))
                                )
                                .concatMap(notification -> notificationRepository.save(notification.toBuilder().lastUpdatedAt(new Date()).status(NotificationStatus.CREATED).build()))
                                .concatMap(notification -> Mono.fromRunnable(() -> System.out.println(notification)))//kafkaTemplate.send(notification.getNotificationService(), notification)))
                )
                .subscribe();
    }

    private boolean isServerParseFileDrop(long currentParsedCount, long absoluteParseCount, long completedCount, Long prevCompletedCount) {
        return currentParsedCount != absoluteParseCount && completedCount == prevCompletedCount;
    }

    private boolean areAllNotificationsSent(long completedCount, long absoluteParseCount) {
        return completedCount == absoluteParseCount;
    }
}
