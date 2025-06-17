package com.vserdiuk.casestudy.messaging;

import com.vserdiuk.casestudy.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * A Spring component that consumes order expiration events from a Kafka topic, processes them
 * by invoking the order expiration logic, and handles errors by sending failed messages to a
 * Dead Letter Queue (DLQ).
 * <p>
 * This class listens to the "order-expiration-topic" Kafka topic, processes the order ID from
 * the message, and calls the {@link OrderService} to expire the corresponding order. If processing
 * fails, the message is sent to the "order-expiration-dlq" topic for further analysis or reprocessing.
 * <p>
 * The consumer operates with manual acknowledgment to ensure reliable message processing and
 * supports configurable concurrency for handling multiple messages in parallel.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationConsumer {

    private final OrderService orderService;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String DLQ_TOPIC = "order-expiration-dlq";

    /**
     * Processes incoming Kafka messages from the "order-expiration-topic" topic.
     * <p>
     * This method is triggered when a message is received from the configured Kafka topic.
     * It attempts to parse the message value as a Long (representing an order ID) and calls
     * the {@link OrderService#expireOrderById(Long)} method to expire the order. If successful,
     * the message is manually acknowledged to confirm processing. If an exception occurs (e.g.,
     * invalid order ID or service failure), the message is sent to the DLQ topic, and the message
     * is still acknowledged to prevent reprocessing.
     *
     * @param record         The Kafka consumer record containing the order ID as a String value.
     * @param acknowledgment The acknowledgment object used to manually acknowledge the message.
     */
    @KafkaListener(
            topics = "order-expiration-topic",
            groupId = "order-expiration-consumer",
            concurrency = "4" // Adjust based on load and resources
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String orderIdStr = record.value();
        try {
            Long orderId = Long.parseLong(orderIdStr);
            log.info("Received expiration event for Order ID: {}", orderId);
            orderService.expireOrderById(orderId);
            acknowledgment.acknowledge(); // Manual acknowledgment
        } catch (Exception e) {
            log.error("Failed to process expiration event for Order ID: {}", orderIdStr, e);
            kafkaTemplate.send(DLQ_TOPIC, orderIdStr); // Send to DLQ
            acknowledgment.acknowledge(); // Acknowledge to avoid reprocessing
        }
    }
}