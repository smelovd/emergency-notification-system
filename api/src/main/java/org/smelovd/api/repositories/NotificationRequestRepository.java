package org.smelovd.api.repositories;

import org.smelovd.api.entities.NotificationRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRequestRepository extends ReactiveMongoRepository<NotificationRequest, String> {
}
