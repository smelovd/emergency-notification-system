package org.smelovd.checker.repositories;

import org.smelovd.checker.entities.NotificationRequestHistory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRequestHistoryRepository extends ReactiveMongoRepository<NotificationRequestHistory, String> {
    Flux<NotificationRequestHistory> findAllByStatus(String status);
}
