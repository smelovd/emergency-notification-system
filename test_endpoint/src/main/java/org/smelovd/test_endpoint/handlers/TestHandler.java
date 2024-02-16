package org.smelovd.test_endpoint.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TestHandler {

    public Mono<ServerResponse> send(ServerRequest request) {
        log.info(request.toString());
        return ServerResponse.ok().build();
    }
}
