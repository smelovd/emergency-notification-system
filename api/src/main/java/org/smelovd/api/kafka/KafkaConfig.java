package org.smelovd.api.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topicTest() {
        return TopicBuilder
                .name("TEST")
                .partitions(8)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic topicSms() {
        return TopicBuilder
                .name("SMS")
                .partitions(8)
                .replicas(1)
                .compact()
                .build();
    }
}
