package org.smelovd.api.repository;


import org.smelovd.api.entity.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {

    <S extends Notification> Flux<S> findAllByTimestampBefore(Mono<Timestamp> timestamp);
}
