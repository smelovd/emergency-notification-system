package org.smelovd.worker.services;

import org.smelovd.worker.entities.NotificationStatus;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<NotificationStatus> send(String serviceUserId, Mono<String> message);
}
