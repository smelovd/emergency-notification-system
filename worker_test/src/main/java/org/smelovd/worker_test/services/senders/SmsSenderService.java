package org.smelovd.worker_test.services.senders;

import org.smelovd.worker_test.entity.NotificationStatus;
import org.smelovd.worker_test.services.senders.senders_metadata.Sender;
import org.springframework.stereotype.Service;

@Service
public class SmsSenderService implements Sender {

    @Override
    public NotificationStatus send(String serviceUserId, String message) {
        System.out.println("SMS \" "+ message +" \" sent to " + serviceUserId);
        return NotificationStatus.DONE;
    }
}
