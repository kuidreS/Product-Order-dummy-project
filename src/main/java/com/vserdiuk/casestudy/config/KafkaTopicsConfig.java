package com.vserdiuk.casestudy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicsConfig {

    public static final String ORDER_EXPIRATION_TOPIC = "order-expiration-topic";

    @Bean
    public KafkaAdmin.NewTopics topics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(ORDER_EXPIRATION_TOPIC)
                        .partitions(1)
                        .replicas(1)
                        .build()
        );
    }
}
