package org.smelovd.checker.repositories;

import org.smelovd.checker.entities.Notification;
import org.smelovd.checker.entities.NotificationStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Date;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {

    Flux<Notification> findAllByNotificationId(String notificationId);
    Flux<Notification> findAllByNotificationIdAndStatus(String notificationId, NotificationStatus status);
    Flux<Notification> findAllByNotificationIdAndStatusAndTimestampBefore(String notificationId, NotificationStatus status, Date timestamp);
}
