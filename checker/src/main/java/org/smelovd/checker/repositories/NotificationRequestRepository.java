package org.smelovd.checker.repositories;

import org.smelovd.checker.entities.NotificationRequest;
import org.smelovd.checker.entities.NotificationRequestStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRequestRepository extends ReactiveMongoRepository<NotificationRequest, String> {

    Flux<NotificationRequest> findAllByStatus(NotificationRequestStatus status);
}
