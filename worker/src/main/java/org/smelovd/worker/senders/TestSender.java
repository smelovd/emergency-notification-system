package org.smelovd.worker.senders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker.exceptions.ServiceSenderException;
import org.smelovd.worker.senders.interfaces.SenderInterface;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestSender implements SenderInterface {

    private static final String URL = "http://172.17.0.1:80/";
    private final WebClient webClient = WebClient.create(URL);

    public Mono<String> send(String serviceUserId, String message) {
        return webClient.post()
                .uri(UriComponentsBuilder.fromUriString(URL + "test")
                        .queryParam("userId", serviceUserId)
                        .queryParam("message", message)
                        .toUriString())
                .exchangeToMono(response -> Mono.just(response.statusCode()))
                .<String>handle((httpStatusCode, synchronousSink) -> {
                    if (httpStatusCode.is2xxSuccessful()) {
                        synchronousSink.next("0");
                    } else if (httpStatusCode.is4xxClientError()) {
                        synchronousSink.next("1");
                    } else if (httpStatusCode.is5xxServerError()) {
                        synchronousSink.error(new ServiceSenderException("Service error", httpStatusCode));
                    } else {
                        log.error("Unknown error: " + httpStatusCode);
                    }
                })
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(5000)))
                .onErrorResume(error -> Mono.just("2"));
    }
}
