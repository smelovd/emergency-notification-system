package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.NotificationRequestStatus;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final RecoveryService recoveryService;

    @Scheduled(fixedRate = 30000)
    public void check() {
        log.info("Start checking for notification requests");

        notificationRequestRepository.findAllByStatus(NotificationRequestStatus.CREATED)
                .flatMap(recoveryService::checkServerParseDown)
                .flatMap(recoveryService::notificationRecovery)
                .subscribe();
    }
}
