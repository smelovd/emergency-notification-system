package org.smelovd.worker_test.services;

import org.smelovd.worker_test.entities.NotificationStatus;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<NotificationStatus> send(String serviceUserId, String message);
}
