package org.smelovd.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.smelovd.api.entities.Notification;
import org.springframework.stereotype.Component;

@Component
public class KafkaSerializer implements Serializer<Notification> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String s, Notification notification) {
        if (notification == null) return null;
        try {
            return objectMapper.writeValueAsBytes(notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("serialization exception", e);
        }
    }
}
