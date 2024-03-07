package org.smelovd.checker.repositories;

import org.smelovd.checker.entities.NotificationRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRequestRepository extends ReactiveMongoRepository<NotificationRequest, String> {
}
