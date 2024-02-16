package org.smelovd.worker_test.services;

import org.smelovd.worker_test.entities.NotificationStatus;
import org.smelovd.worker_test.services.exceptions.ServiceException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import static org.smelovd.worker_test.entities.NotificationStatus.*;

@Service
public class TestSenderService implements NotificationService {

    private static final String URL = "http://test-endpoint:8080/test/send";
    private final WebClient webClient = WebClient.create(URL);

    public Mono<NotificationStatus> send(String serviceUserId, String message) {
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> Mono.just(response.statusCode()))
                .<NotificationStatus>handle((httpStatusCode, synchronousSink) -> {
                    if (httpStatusCode.is2xxSuccessful()) synchronousSink.next(DONE);
                    else if (httpStatusCode.is5xxServerError()) synchronousSink.error(new ServiceException("Service error", httpStatusCode));
                    else if (httpStatusCode.is4xxClientError()) synchronousSink.next(CLIENT_ERROR);
                })
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(500))
                        .filter(throwable -> throwable instanceof ServiceException))
                .onErrorResume(error -> Mono.just(SERVER_ERROR));
    }
}
