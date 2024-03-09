package org.smelovd.worker.repositories;

import org.smelovd.worker.entities.NotificationTemplate;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTemplateRepository extends ReactiveMongoRepository<NotificationTemplate, String> {
}
