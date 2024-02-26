package org.smelovd.checker.repositories;

import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {

    Flux<Notification> findAllByRequestId(String requestId);
    Flux<Notification> findAllByRequestIdAndStatus(String requestId, NotificationStatus status);
    Flux<Notification> findAllByRequestIdAndStatusAndLastUpdatedAtBefore(String notificationId, NotificationStatus status, Date lastUpdatedAt);
    Mono<Notification> findByServiceUserIdAndNotificationService(String serviceUserId, String notificationService);
}
