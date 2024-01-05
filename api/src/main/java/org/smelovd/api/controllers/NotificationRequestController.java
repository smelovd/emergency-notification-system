package org.smelovd.api.controllers;

import org.smelovd.api.entity.Notification;
import org.smelovd.api.entity.NotificationRequest;
import org.smelovd.api.repository.NotificationRepository;
import org.smelovd.api.service.NotificationRequestService;
import org.smelovd.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationRequestController {

    private final NotificationRequestService notificationRequestService;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Notification> kafkaTemplate;

    @PostMapping("/send-notification")
    public ResponseEntity<?> saveNotificationRequest(@RequestParam("message") String message, @RequestParam("file") MultipartFile file) {
        System.out.println("sending notification");
        log.info("try to send notification \"{}\", to users in file \"{}\"",
                message, file.getOriginalFilename());
        NotificationRequest request = notificationRequestService.saveRequest(message, file).block();
        log.info("saved request " + request);
        try {
            notificationRepository.insert(notificationService.getFluxNotifications(file, request.getId()))
                    .publishOn(Schedulers.boundedElastic())
                    .log()
                    .subscribe(n -> kafkaTemplate.send("notifications", n));

            return new ResponseEntity<>("notifications add to queue", HttpStatus.OK);
        } catch (IOException | InterruptedException e) {
            return new ResponseEntity<>("file or message is invalid", HttpStatus.BAD_REQUEST);
        }

    }
}
