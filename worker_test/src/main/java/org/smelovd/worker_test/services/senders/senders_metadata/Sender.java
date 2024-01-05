package org.smelovd.worker_test.services.senders.senders_metadata;

import org.smelovd.worker_test.entity.NotificationStatus;


public interface Sender {

    NotificationStatus send(String serviceUserId, String message);
}
