package org.smelovd.worker.services.senders.interfaces;

import reactor.core.publisher.Mono;

public interface NotificationServiceInterface {
    Mono<String> send(String serviceUserId, String message);
}
