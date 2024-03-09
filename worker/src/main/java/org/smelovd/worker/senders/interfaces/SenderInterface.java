package org.smelovd.worker.senders.interfaces;

import reactor.core.publisher.Mono;

public interface SenderInterface {
    Mono<String> send(String serviceUserId, String message);
}
