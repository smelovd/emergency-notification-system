package org.smelovd.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.repositories.NotificationRepository;
import org.smelovd.api.repositories.NotificationRequestRepository;
import org.smelovd.api.repositories.NotificationTemplateRepository;
import org.smelovd.api.repositories.cache.NotificationCacheRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryProduceService {

    private final ProduceService produceService;
    private final NotificationCacheRepository notificationCacheRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationRequestRepository notificationRequestRepository;

    public Mono<Void> asyncParseAndProduce(String requestId, String currentParsedCount) {
        return Mono.fromRunnable(() -> produceService.produceFromFile(requestId, Long.valueOf(currentParsedCount)).subscribe());
    }

    public Mono<Void> asyncProduce(String requestId) {
        return Mono.fromRunnable(() -> notificationCacheRepository.findAllIdsByRequestId(requestId).collectList()
                .flatMapMany(producedNotifications -> notificationTemplateRepository.findById(notificationRequestRepository.findById(requestId).map(NotificationRequest::getTemplateId))
                        .flatMap(template -> notificationRepository.findAllByTemplateId(template.getId()).collectList()
                                .map(allNotification -> allNotification.stream().filter(n -> !producedNotifications.contains(n.getId())).collect(Collectors.toList()))
                                .flatMapIterable(notProducedNotifications -> notProducedNotifications)
                                .doOnNext(produceService::pushToQueue).then()))
                .subscribe());
    }
}
