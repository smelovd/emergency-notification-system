package org.smelovd.worker.services.senders.interfaces;

import org.smelovd.worker.entities.NotificationStatus;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<NotificationStatus> send(String serviceUserId, String requestId);
}
