package org.smelovd.worker_test.services.senders;

import org.smelovd.worker_test.entity.NotificationStatus;
import org.smelovd.worker_test.services.senders.senders_metadata.Sender;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.smelovd.worker_test.entity.NotificationStatus.DONE;
import static org.smelovd.worker_test.entity.NotificationStatus.ERROR;

@Service
public class TestSenderService implements Sender {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String url = "http://test-endpoint:8080/test/send";

    @Override
    public NotificationStatus send(String serviceUserId, String message) {
        String request =
                "{\"id\": \"" + serviceUserId + "\", " +
                "\"message\": \"" + message + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(request);
        ResponseEntity<?> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Void.class
        );
        if (response.getStatusCode().is2xxSuccessful()) return DONE;
        //if (response.getStatusCode().is5xxServerError()) send(serviceUserId, message);
        return ERROR; //TODO timeout / external server problem
    }
}
