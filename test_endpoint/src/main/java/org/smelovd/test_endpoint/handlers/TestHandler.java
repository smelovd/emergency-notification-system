package org.smelovd.test_endpoint.handlers;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class TestHandler {

    private final AtomicLong countRequest = new AtomicLong(0);

    public Mono<ServerResponse> send(ServerRequest request) {
        System.out.println(countRequest.incrementAndGet());
        return ServerResponse.ok().build();
    }


}
