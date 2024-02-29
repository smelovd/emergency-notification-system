package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryService {

    private static final String URL = "http://172.17.0.1:80/";
    private final WebClient webClient = WebClient.create();

    public Mono<String> fileRecoveryRequestSend(String requestId, Long currentParsedCount) {

        return webClient.post()
                .uri(UriComponentsBuilder.fromUriString(URL + "/api/send/recovery")
                        .queryParam("requestId", requestId)
                        .queryParam("currentParsedCount", String.valueOf(currentParsedCount))
                        .toUriString())
                .retrieve()
                .bodyToMono(String.class);
                //.retryWhen(Retry.fixedDelay(3, Duration.ofMillis(10000)));
    }
}
