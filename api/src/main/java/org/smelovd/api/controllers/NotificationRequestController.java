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

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationRequestController {

    private final NotificationRequestService notificationRequestService;
    private final NotificationService notificationService;

    @PostMapping("/send")
    public Mono<ResponseEntity<NotificationRequest>> saveAndProduceNotificationRequest(@RequestPart("message") String message, @RequestPart("file") Mono<FilePart> file) {
        return notificationRequestService.save(message, file)
                .flatMap(request -> notificationService.asyncProduce(request)
                .thenReturn(new ResponseEntity<>(request, HttpStatus.OK)));
    }

    @PostMapping(value = "/send/recovery")
    public Mono<ResponseEntity<String>> recoveryProduceNotificationRequest(@RequestParam("requestId") String requestId, @RequestParam("currentParsedCount") String currentParsedCount) {
        return notificationService.recoveryAsyncProduce(requestId, currentParsedCount)
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
    }
}
