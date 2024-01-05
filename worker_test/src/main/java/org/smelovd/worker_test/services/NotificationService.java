package org.smelovd.worker_test.services;

import org.smelovd.worker_test.entity.Notification;
import org.smelovd.worker_test.entity.NotificationStatus;
import org.smelovd.worker_test.services.senders.MailSenderService;
import org.smelovd.worker_test.services.senders.SmsSenderService;
import org.smelovd.worker_test.services.senders.TestSenderService;
import org.smelovd.worker_test.services.senders.senders_metadata.Sender;
import org.smelovd.worker_test.services.senders.senders_metadata.SenderServiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

import static org.smelovd.worker_test.services.senders.senders_metadata.SenderServiceType.*;

@Service
@Slf4j
public class NotificationService {

    private final Map<SenderServiceType, Sender> senderServiceMap;
    private final CachingService cachingService;

    @Autowired
    public NotificationService(MailSenderService mailSenderService, SmsSenderService smsSenderService, TestSenderService testSenderService, CachingService cachingService) {
        this.cachingService = cachingService;

        this.senderServiceMap = new EnumMap<>(SenderServiceType.class);
        senderServiceMap.put(SMS, smsSenderService);
        senderServiceMap.put(MAIL, mailSenderService);
        senderServiceMap.put(TEST, testSenderService);
    }

    public NotificationStatus send(Notification notification) {
        Sender sender = senderServiceMap.get(notification.getNotificationService());
        return sender.send(notification.getServiceUserId(),
                cachingService.getMessageByRequestId(notification.getNotificationId()));
    }
}
