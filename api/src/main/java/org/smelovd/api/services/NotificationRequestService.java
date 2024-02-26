package org.smelovd.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.repositories.NotificationRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.Date;

import static org.smelovd.api.entities.NotificationRequestStatus.CREATED;
import static org.smelovd.api.entities.NotificationRequestStatus.CREATING;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRequestService {

    @Value("${base-file-path}")
    private String BASE_FILE_PATH;
    private final NotificationRequestRepository notificationRequestRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<NotificationRequest> save(String message, Mono<FilePart> file) {

        return file.flatMap(filePart ->
                notificationRequestRepository.insert(
                                NotificationRequest.builder()
                                        .message(message)
                                        .createdAt(new Date())
                                        .status(CREATING)
                                        .build())
                        .flatMap(request -> filePart.transferTo(new File(BASE_FILE_PATH + request.getId() + ".csv"))
                                .then(Mono.fromCallable(() -> getNotificationCount(BASE_FILE_PATH + request.getId() + ".csv")))
                                .flatMap(notificationCount -> notificationRequestRepository.save(request.toBuilder()
                                        .status(CREATED)
                                        .notificationCount(notificationCount)
                                        .build()))
                        ));
    }

    private Long getNotificationCount(String filePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            return reader.lines().count();
        } catch (FileNotFoundException e) {
            log.error("file not found " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("file close/open error " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
