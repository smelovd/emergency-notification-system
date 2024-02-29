package org.smelovd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.services.NotificationRequestService;
import org.smelovd.api.services.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationRequestController {

    private final NotificationRequestService notificationRequestService;
    private final NotificationService notificationService;

    @PostMapping("/send")
    public Mono<ResponseEntity<NotificationRequest>> saveAndProduceNotificationRequest(@RequestParam("message") String message, @RequestPart("file") Mono<FilePart> file) {
        log.info("saving notification with message \"{}\"", message);
        return notificationRequestService.save(message, file)
                .flatMap(request -> {
                    log.info("saved request " + request);
                    return Mono.fromRunnable(() -> notificationService.produce(request.getId())
                                    .subscribeOn(Schedulers.boundedElastic()).subscribe())
                            .thenReturn(new ResponseEntity<>(request, HttpStatus.OK));
                });
    }

    @PostMapping(value = "/send/recovery")
    public Mono<ResponseEntity<String>> recoveryProduceNotificationRequest(@RequestParam("requestId") String requestId, @RequestParam("currentParsedCount") String currentParsedCount) {
        log.info("recovery notification with request id: {}", requestId);
        return Mono.fromRunnable(() -> notificationService.produce(requestId, Long.valueOf(currentParsedCount))
                        .subscribeOn(Schedulers.boundedElastic()).subscribe())
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
    }
}
