package org.smelovd.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.entities.NotificationRequestStatus;
import org.smelovd.api.repositories.NotificationRequestRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRequestService {

    private final NotificationRequestRepository notificationRequestRepository;
    private final TransactionalOperator transactionalOperator;

    private static final String BASE_FILE_PATH = "";

    //@Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<Void> save(String message, Mono<FilePart> file) {
//        return notificationRequestRepository.insert(NotificationRequest.builder() //TODO self generate 'id' for request
//                        .message(message)
//                        .timestamp(new Date())
//                        .status(NotificationRequestStatus.CREATED)
//                        .build())
//                //.flatMap(this.notificationRequestRepository::insert)
//                .flatMap(request -> file.
//                        flatMap(filePart -> filePart.transferTo(new File(BASE_FILE_PATH + request.getId() + ".csv")))
//                                .flatMap()
//                            //.as(this.transactionalOperator::transactional);
//
////                var transaction = Mono
//                .just(NotificationRequest.builder() //TODO self generate 'id' for request
//                        .message(message)
//                        .timestamp(new Date())
//                        .status(NotificationRequestStatus.CREATED)
//                        .build())
//                .flatMap(this.notificationRequestRepository::insert)
//                .zipWith(file)
//                .flatMap(this::saveFile)
//
//                .as(this.transactionalOperator::transactional);

        //return transactionalOperator.execute(status -> transaction);





        return notificationRequestRepository.insert(NotificationRequest.builder() //TODO self generate 'id' for request
                        .message(message)
                        .timestamp(new Date())
                        .status(NotificationRequestStatus.CREATED)
                        .build())
                .flatMap(request -> file.
                        flatMap(filePart ->
                                filePart.transferTo(new File(BASE_FILE_PATH + request.getId() + ".csv"))).then());
    }
//
//    @Transactional(isolation = Isolation.READ_COMMITTED)
//    public Mono<NotificationRequest> saveFile(Tuple2<NotificationRequest, FilePart> tuple) {
//        NotificationRequest request = tuple.getT1();
//        FilePart filePart = tuple.getT2();
//        String filePath = request.getId() + ".csv";
//
//        return filePart.transferTo(new File(filePath))
//                .then(Mono.fromRunnable(() -> {
//                    Mono<NotificationRequest> saveRequestMono = Mono.just(request.toBuilder().filePath(filePath).notificationCount(getNotificationCount(filePath)).build())
//                            .doOnNext(savedRequest -> {
//                                Assert.isTrue((new File(request.getId())).exists(), "File save error");
//                                Assert.isTrue(request.getNotificationCount() != 0L, "Notification count have to be at least 1");
//                            })
//                            .flatMap(notificationRequestRepository::save)
//                            .as(this.transactionalOperator::transactional);
//                }));
//    }

    private long getNotificationCount(String filePath) {
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
