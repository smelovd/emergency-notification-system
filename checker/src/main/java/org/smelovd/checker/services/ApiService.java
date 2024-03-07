package org.smelovd.checker.services;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@NoArgsConstructor
public class ApiService {

    private final String URL = "http://172.17.0.1:80/";
    private final WebClient webClient = WebClient.create();

    public Mono<Void> sendRecovery(String requestId, boolean isParsed, long currentParsedCount) {

        return webClient.post()
                .uri(UriComponentsBuilder.fromUriString(URL + "/api/send/recovery/" + requestId)
                        .queryParam("isParsed", isParsed)
                        .queryParam("currentParsedCount", String.valueOf(currentParsedCount))
                        .toUriString())
                .retrieve()
                .bodyToMono(String.class)
                .retry(5)
                .then();
    }
}
