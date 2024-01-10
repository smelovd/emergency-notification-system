package org.smelovd.worker_test.services;

import lombok.RequiredArgsConstructor;
import org.smelovd.worker_test.entity.Notification;
import org.smelovd.worker_test.entity.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final TestSenderService smsSenderService;
    private final CachingService cachingService;

    public NotificationStatus send(Notification notification) {
        return smsSenderService.send(
                notification.getServiceUserId(),
                cachingService.getMessageByRequestId(notification.getNotificationId())
        );
    }
}
