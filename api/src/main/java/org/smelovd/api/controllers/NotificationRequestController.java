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
@RequestMapping("/api/send")
@RequiredArgsConstructor
public class NotificationRequestController {

    private final NotificationRequestService notificationRequestService;
    private final NotificationService notificationService;

    @PostMapping("")
    public Mono<ResponseEntity<NotificationRequest>> parseAndProduce(@RequestPart("message") String message, @RequestPart("file") Mono<FilePart> file) {
        return notificationRequestService.save(message, file)
                .flatMap(request -> {
                    log.info("Send recovery notification with id: {}", request.getId());
                    return notificationService.asyncParseAndProduce(request.getId())
                            .thenReturn(new ResponseEntity<>(request, HttpStatus.OK));
                });
    }

    @PostMapping("/{requestId}")
    public Mono<ResponseEntity<String>> produce(@PathVariable("requestId") String requestId) {
        log.info("Send recovery notification with id: {}", requestId);
        return notificationService.asyncProduce(requestId)
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
    }
}
