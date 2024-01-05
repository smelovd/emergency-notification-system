package org.smelovd.worker_test.repositories;

import org.smelovd.worker_test.entity.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
}
