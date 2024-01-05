package org.smelovd.worker_test.services;

import org.smelovd.worker_test.entity.NotificationRequest;
import org.smelovd.worker_test.repositories.NotificationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class CachingService {

    private final NotificationRequestRepository notificationRequestRepository;

    @Cacheable(key = "#id", value = "message")
    public String getMessageByRequestId(String id) {
        System.out.println(LocalTime.now() + "get message from request by id: " + id);
        return notificationRequestRepository.findById(id)
                .map(NotificationRequest::getMessage).block();
    }
}
