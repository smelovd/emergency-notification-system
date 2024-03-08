package org.smelovd.api.factories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationTemplate;
import org.smelovd.api.repositories.NotificationTemplateRepository;
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
public class NotificationTemplateFactory {

    @Value("${base-file-path}")
    private String BASE_FILE_PATH;
    private final NotificationTemplateRepository notificationTemplateRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<NotificationTemplate> create(String message, Mono<FilePart> file) {
        log.info("saving notification with message \"{}\"", message);
        return file.flatMap(filePart -> insert(message)
                .flatMap(template -> saveFile(template, filePart)));
    }

    private Mono<NotificationTemplate> insert(String message) {
        return notificationTemplateRepository.insert(
                NotificationTemplate.builder()
                        .message(message)
                        .createdAt(new Date())
                        .isParsed(false)
                        .build());
    }

    private Mono<NotificationTemplate> saveFile(NotificationTemplate template, FilePart filePart) {
        return filePart.transferTo(new File(BASE_FILE_PATH + template.getId() + ".csv"))
                .then(Mono.defer(() -> this.updateCountById(template.getId(), getNotificationCount(template.getId()))));
    }

    private Mono<NotificationTemplate> updateCountById(String id, Long notificationCount) {
        return notificationTemplateRepository.findById(id)
                .flatMap(request -> Mono.just(request.toBuilder().notificationCount(notificationCount).build()))
                .flatMap(notificationTemplateRepository::save);
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
}
