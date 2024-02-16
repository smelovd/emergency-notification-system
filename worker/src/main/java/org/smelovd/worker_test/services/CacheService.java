package org.smelovd.worker_test.services;

import lombok.extern.slf4j.Slf4j;
import org.smelovd.worker_test.entities.NotificationRequest;
import org.smelovd.worker_test.repositories.NotificationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final NotificationRequestRepository notificationRequestRepository;

    //@CachePut(key = "#id", value = "message", cacheNames = "request_messages")
    public String getMessageByRequestId(String id) {
        log.info("get message from request by id: " + id);
        return notificationRequestRepository.findById(id)
                .map(NotificationRequest::getMessage).block();
    }
}
