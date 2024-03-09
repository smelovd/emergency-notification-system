package org.smelovd.worker.repositories;

import org.smelovd.worker.entities.NotificationRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRequestRepository extends ReactiveMongoRepository<NotificationRequest, String> {
    Flux<NotificationRequest> findAllByStatus(String status);
}
