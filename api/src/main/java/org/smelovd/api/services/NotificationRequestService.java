package org.smelovd.api.services;

import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.entities.NotificationRequestStatus;
import org.smelovd.api.repositories.NotificationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRequestService {

    private final NotificationRequestRepository notificationRequestRepository;

    public Mono<NotificationRequest> save(String message, MultipartFile file) {
        log.info("saving request \"{}\", \"{}\"", message, file.getOriginalFilename());
        return notificationRequestRepository.insert(buildNotificationRequest(message, file));
    }

    private NotificationRequest buildNotificationRequest(String message, MultipartFile file) {
        return NotificationRequest.builder() //TODO filepath
                .message(message)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .status(NotificationRequestStatus.CREATED)
                .build();
    }

}
