package org.smelovd.worker_test.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.smelovd.worker_test.entities.Notification;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class KafkaDeserializer extends JsonDeserializer<Notification> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        super.configure(configs, isKey);
    }

    @Override
    public Notification deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, Notification.class);
        } catch (IOException e) {
            System.out.println("KafkaDeserializer has problem");
            throw new RuntimeException("Exception deserialization", e);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}