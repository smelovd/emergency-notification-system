package org.smelovd.checker.repositories;

import org.smelovd.checker.entities.NotificationTemplate;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTemplateRepository extends ReactiveMongoRepository<NotificationTemplate, String> {
}
