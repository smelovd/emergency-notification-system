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

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRequestService {

    @Value("${base-file-path}")
    private String BASE_FILE_PATH;
    private final NotificationRequestRepository notificationRequestRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<NotificationRequest> save(String message, Mono<FilePart> file) {
        log.info("saving notification with message \"{}\"", message);
        return file.flatMap(filePart -> insertRequestToDatabase(message)
                .flatMap(request -> saveFile(request, filePart)));
    }

    private Mono<NotificationRequest> insertRequestToDatabase(String message) {
        return notificationRequestRepository.insert(
                NotificationRequest.builder()
                        .message(message)
                        .createdAt(new Date())
                        .isParsed(false)
                        .build());
    }

    private Mono<NotificationRequest> saveFile(NotificationRequest request, FilePart filePart) {
        return filePart.transferTo(new File(BASE_FILE_PATH + request.getId() + ".csv"))
                .then(Mono.defer(() -> this.updateCountById(request.getId(), getNotificationCount(request.getId()))));
    }

    private Mono<NotificationRequest> updateCountById(String id, Long notificationCount) {
        return notificationRequestRepository.findById(id)
                .flatMap(request -> Mono.just(request.toBuilder().notificationCount(notificationCount).build()))
                .flatMap(notificationRequestRepository::save);
    }

    private Long getNotificationCount(String id) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream( BASE_FILE_PATH + id + ".csv")))) {
            return reader.lines().count();
        } catch (FileNotFoundException e) {
            log.error("file not found " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("file close/open error " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Mono<Void> updateIsParsed(String requestId, boolean isParsed) {
        return notificationRequestRepository.findById(requestId)
                .flatMap(request -> Mono.just(request.toBuilder().isParsed(isParsed).build()))
                .flatMap(notificationRequestRepository::save).then();
    }
}
