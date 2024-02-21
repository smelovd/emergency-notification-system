package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.NotificationRequestStatus;
import org.smelovd.checker.entities.NotificationStatus;
import org.smelovd.checker.repositories.NotificationRepository;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.smelovd.checker.services.exceptions.ServerFileParseDropException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
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

    @Scheduled(fixedRate = 10000)
    public void time() {
        System.out.println(LocalTime.now());
    }

    @Scheduled(fixedRate = 20000)
    public void checkAvailability() {
        notificationRequestRepository.findAllByStatus(NotificationRequestStatus.CREATED)
                .concatMap(request -> Mono.zip(
                                notificationRepository.findAllByNotificationIdAndStatus(request.getId(), NotificationStatus.DONE).count(),
                                notificationRepository.findAllByNotificationIdAndStatus(request.getId(), NotificationStatus.CLIENT_ERROR).count(),
                                notificationRepository.findAllByNotificationId(request.getId()).count()
                        )
                        .doOnNext(System.out::println)
                        .flatMap(tuple -> {
                            long producedCount = tuple.getT1() + tuple.getT2();
                            long currentAbsoluteCount = tuple.getT3();
                            long absoluteCount = request.getNotificationCount();

                            if (currentAbsoluteCount == absoluteCount) {
                                if (producedCount == absoluteCount) {
                                    return notificationRequestRepository.save(request.toBuilder().status(NotificationRequestStatus.DONE).build());
                                }

                            } else if (producedCount == prevCompletedCount.getOrDefault(request.getId(), -1L)) {
                                return Mono.error(new ServerFileParseDropException()); //TODO
                            }
                            prevCompletedCount.put(request.getId(), producedCount);
                            return Mono.just(request);
                        }))
                //.doOnError() //TODO

                .concatMap(request -> Flux.merge(
                                        notificationRepository.findAllByNotificationIdAndStatus(request.getId(), NotificationStatus.SERVER_ERROR),
                                        notificationRepository.findAllByNotificationIdAndStatusAndTimestampBefore(request.getId(), NotificationStatus.CREATED, new Date(System.currentTimeMillis() - 60000L))
                                )
                                .concatMap(notification -> notificationRepository.save(notification.toBuilder().timestamp(new Date()).status(NotificationStatus.CREATED).build()))
                                .concatMap(notification -> Mono.fromRunnable(() -> System.out.println(notification)))//kafkaTemplate.send(notification.getNotificationService(), notification)))
                )
                .subscribe();
    }
}
