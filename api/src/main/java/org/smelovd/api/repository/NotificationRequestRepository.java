package org.smelovd.api.repository;

import org.smelovd.api.entity.NotificationRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRequestRepository extends ReactiveMongoRepository<NotificationRequest, String> {
}
