package org.smelovd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.services.NotificationRequestService;
import org.smelovd.api.services.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationRequestController {

    private final NotificationRequestService notificationRequestService;
    private final NotificationService notificationService;

    @PostMapping("/send-notification")
    public Mono<ResponseEntity<NotificationRequest>> saveNotificationRequest(@RequestParam("message") String message, @RequestParam("file") MultipartFile file) {
        log.info("saving notification \"{}\", to users in file \"{}\"", message, file.getOriginalFilename());

        return notificationRequestService.save(message, file)
                .flatMap(request -> {
                    log.info("saved request " + request);
                    return notificationService.pushToDatabaseAndQueue(file, request.getId())
                            .thenReturn(new ResponseEntity<>(request, HttpStatus.OK));
                });
    }

    @ExceptionHandler
    public ResponseEntity<NotificationRequest> handler() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
