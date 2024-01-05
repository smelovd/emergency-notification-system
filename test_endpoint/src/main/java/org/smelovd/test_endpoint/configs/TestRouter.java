package org.smelovd.test_endpoint.configs;

import org.smelovd.test_endpoint.handlers.TestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration(proxyBeanMethods = false)
public class TestRouter {

    @Bean
    public RouterFunction<ServerResponse> route(TestHandler testHandler) {
        return RouterFunctions.route(POST("/test/send").and(accept(MediaType.APPLICATION_JSON)),
                testHandler::send);
    }
}
