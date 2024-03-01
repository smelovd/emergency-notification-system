package org.smelovd.checker.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.entities.NotificationRequestStatus;
import org.smelovd.checker.entities.NotificationStatus;
import org.smelovd.checker.repositories.NotificationRepository;
import org.smelovd.checker.repositories.NotificationRequestRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryService {

    private static final String URL = "http://172.17.0.1:80/";
    private final WebClient webClient = WebClient.create();
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final NotificationRequestRepository notificationRequestRepository;
    private static final Map<String, Long> prevCompletedCount = new HashMap<>();

    public Mono<NotificationRequest> fileRecoveryRequestSend(NotificationRequest request, Long currentParsedCount) {

        return webClient.post()
                .uri(UriComponentsBuilder.fromUriString(URL + "/api/send/recovery")
                        .queryParam("requestId", request.getId())
                        .queryParam("currentParsedCount", String.valueOf(currentParsedCount))
                        .toUriString())
                .retrieve()
                .bodyToMono(String.class)
                .thenReturn(request); //TODO maybe retry
    }

    public Flux<Object> notificationRecovery(NotificationRequest request) {
        return Flux.merge(
                        notificationRepository.findAllByRequestIdAndStatus(request.getId(), NotificationStatus.SERVER_ERROR),
                        notificationRepository.findAllByRequestIdAndStatusAndLastUpdatedAtBefore(request.getId(), NotificationStatus.CREATED, new Date(System.currentTimeMillis() - 60000L))
                )
                .concatMap(notification -> notificationRepository.save(
                        notification.toBuilder()
                                .lastUpdatedAt(new Date())
                                .status(NotificationStatus.CREATED)
                                .build()))
                .concatMap(notification -> Mono.fromRunnable(() -> kafkaTemplate.send(notification.getNotificationService(), notification))
                        .thenReturn(request));
    }

    public Mono<NotificationRequest> checkServerParseDown(NotificationRequest request) {
        return getNotificationCounts(request)
                .flatMap(counts -> {
                    long completedCount = counts.getT1() + counts.getT2();
                    long currentParsedCount = counts.getT3();
                    long absoluteParseCount = request.getNotificationCount();

                    if (areAllMessagesSent(completedCount, absoluteParseCount)) { //TODO replace
                        prevCompletedCount.remove(request.getId());
                        log.info("all messages sent, changing status to DONE, request id: " + request.getId());
                        return notificationRequestRepository.save(request.toBuilder().status(NotificationRequestStatus.DONE).build());
                    }

                    if (isServerParseDown(currentParsedCount, absoluteParseCount, completedCount, prevCompletedCount.getOrDefault(request.getId(), -1L))) {
                        log.info("Server parsing file is down, initiating async recovery");
                        return fileRecoveryRequestSend(request, currentParsedCount);
                    }

                    prevCompletedCount.put(request.getId(), completedCount);
                    return Mono.just(request);
                });
    }

    private Mono<Tuple3<Long, Long, Long>> getNotificationCounts(NotificationRequest request) {
        return Mono.zip(
                notificationRepository.findAllByRequestIdAndStatus(request.getId(), NotificationStatus.DONE).count(),
                notificationRepository.findAllByRequestIdAndStatus(request.getId(), NotificationStatus.CLIENT_ERROR).count(),
                notificationRepository.findAllByRequestId(request.getId()).count()
        );
    }

    private boolean isServerParseDown(long currentParsedCount, long absoluteParseCount, long completedCount, Long prevCompletedCount) {
        return currentParsedCount != absoluteParseCount && completedCount == prevCompletedCount;
    }

    private boolean areAllMessagesSent(long completedCount, long absoluteParseCount) {
        return completedCount == absoluteParseCount;
    }
}
