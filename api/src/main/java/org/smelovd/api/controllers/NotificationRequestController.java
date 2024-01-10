package org.smelovd.api.controllers;

import org.smelovd.api.entity.NotificationRequest;
import org.smelovd.api.service.NotificationRequestService;
import org.smelovd.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationRequestController {

    private final NotificationRequestService notificationRequestService;
    private final NotificationService notificationService;

    @PostMapping("/send-notification")
    public NotificationRequest saveNotificationRequest(@RequestParam("message") String message, @RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        log.info("saving notification \"{}\", to users in file \"{}\"", message, file.getOriginalFilename());
        var request = notificationRequestService.saveRequest(message, file).blockOptional().orElseThrow();
        log.info("saved request " + request);

        notificationService.pushNotifications(file, request.getId());
        return request;
    }
 }
