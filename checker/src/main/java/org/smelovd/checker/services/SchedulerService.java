package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.NotificationRequestHistory;
import org.smelovd.checker.repositories.NotificationRequestHistoryRepository;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Possible problems:

 *  Request by file:
 * 1) api down, file parsing error, push to queue not all;
 *  Request by id:
 * 2) api down, push to queue not all

 *  Sender problems:
 * 3) status 2 (SERVER_ERROR), sender without response;
 * 4) worker down (lost some messages) - same fix with 2-nd point
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final RecoveryService recoveryService;
    private final NotificationRequestHistoryRepository notificationRequestHistoryRepository;
    private final NotificationRequestService notificationRequestService;
    private final NotificationRequestRepository notificationRequestRepository;

    @Scheduled(fixedDelay = 60000)
    public void recovery() {
        log.info("Start checking for problem");
        notificationRequestHistoryRepository.findAllByStatus("CREATED")
                .buffer(100)
                .concatMap(historyRequest -> notificationRequestRepository.findAllById(historyRequest.stream()
                        .map(NotificationRequestHistory::getRequestId).collect(Collectors.toSet())))
                .concatMap(notificationRequestService::updateAlreadySentStatus)

                .concatMap(recoveryService::fixServerDown)
                .concatMap(recoveryService::produceServerErrorNotifications)

                .subscribe();
    }
}
