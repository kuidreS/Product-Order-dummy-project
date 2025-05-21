package com.vserdiuk.casestudy.messaging;

import com.vserdiuk.casestudy.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "order-expiration-topic", groupId = "order-expiration-consumer")
    public void onMessage(String orderIdStr) {
        try {
            var orderId = Long.parseLong(orderIdStr);
            log.info("Received expiration event for Order ID: {}", orderId);
            orderService.expireOrderById(orderId);
        } catch (Exception e) {
            log.error("Failed to process expiration event: {}", orderIdStr, e);
        }
    }
}
