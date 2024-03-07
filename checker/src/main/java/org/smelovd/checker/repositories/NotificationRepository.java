package org.smelovd.checker.repositories;

import org.smelovd.checker.entities.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
    Flux<Notification> findAllByTemplateId(String templateId);

    Mono<Long> countAllByTemplateId(String templateId);
}
