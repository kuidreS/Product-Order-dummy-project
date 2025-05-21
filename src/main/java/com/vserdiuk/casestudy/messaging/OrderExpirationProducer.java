package com.vserdiuk.casestudy.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.vserdiuk.casestudy.config.KafkaTopicsConfig.ORDER_EXPIRATION_TOPIC;

@Component
@RequiredArgsConstructor
public class OrderExpirationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Async
    public void scheduleExpiration(Long orderId, long delayMinutes) {
        try {
            TimeUnit.MINUTES.sleep(delayMinutes);
            kafkaTemplate.send(ORDER_EXPIRATION_TOPIC, orderId.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
