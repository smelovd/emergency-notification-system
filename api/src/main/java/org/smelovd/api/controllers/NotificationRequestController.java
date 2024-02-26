package org.smelovd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.services.NotificationRequestService;
import org.smelovd.api.services.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationRequestController {

    private final NotificationRequestService notificationRequestService;
    private final NotificationService notificationService;

    @PostMapping("/send-notification")
    public Mono<ResponseEntity<NotificationRequest>> saveAndProduceNotificationRequest(@RequestPart("message") String message, @RequestPart("file") Mono<FilePart> file) {
        log.info("saving notification \"{}\", to users in file", message);
        return notificationRequestService.save(message, file)
                .flatMap(request -> {
            log.info("saved request " + request);
            return Mono.fromRunnable(() -> notificationService
                            .produce(request.getId())
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe())
                    .thenReturn(new ResponseEntity<>(request, HttpStatus.OK));
        });
    }
}
