package com.vserdiuk.casestudy.messaging;

import com.vserdiuk.casestudy.entity.OrderExpirationTask;
import com.vserdiuk.casestudy.entity.TaskStatus;
import com.vserdiuk.casestudy.repository.OrderExpirationTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static com.vserdiuk.casestudy.config.KafkaTopicsConfig.ORDER_EXPIRATION_TOPIC;

/**
 * A Spring component responsible for producing order expiration events to a Kafka topic.
 * This class schedules and processes order expiration tasks, sending events for expired orders
 * to a Kafka topic and updating the task status accordingly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderExpirationTaskRepository taskRepository;

    /**
     * Schedules an order expiration task for a given order ID with a specified delay.
     * Creates a new {@link OrderExpirationTask} with a pending status and saves it to the repository.
     *
     * @param orderId      the ID of the order to schedule for expiration
     * @param delayMinutes the delay in minutes before the order expires
     */
    public void scheduleExpiration(Long orderId, long delayMinutes) {
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(delayMinutes);
        OrderExpirationTask task = OrderExpirationTask.builder()
                .orderId(orderId)
                .expirationTime(expirationTime)
                .status(TaskStatus.PENDING)
                .build();
        taskRepository.save(task);
        log.info("Scheduled expiration task for Order ID: {} at {}", orderId, expirationTime);
    }

    /**
     * Periodically processes pending order expiration tasks that have reached their expiration time.
     * Runs every minute to check for tasks with a status of {@link TaskStatus#PENDING} and an
     * expiration time before the current time. Sends expiration events to the Kafka topic
     * and updates the task status to {@link TaskStatus#SENT} or {@link TaskStatus#FAILED}
     * based on the outcome of the Kafka send operation.
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processExpiredTasks() {
        LocalDateTime now = LocalDateTime.now();
        taskRepository.findByStatusAndExpirationTimeBefore(TaskStatus.PENDING, now)
                .forEach(task -> {
                    try {
                        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(ORDER_EXPIRATION_TOPIC, task.getOrderId().toString());
                        future.whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send expiration event for Order ID: {}", task.getOrderId(), ex);
                                task.setStatus(TaskStatus.FAILED);
                            } else {
                                log.info("Sent expiration event for Order ID: {}", task.getOrderId());
                                task.setStatus(TaskStatus.SENT);
                            }
                            taskRepository.save(task);
                        });
                    } catch (Exception e) {
                        log.error("Error processing expiration task for Order ID: {}", task.getOrderId(), e);
                        task.setStatus(TaskStatus.FAILED);
                        taskRepository.save(task);
                    }
                });
    }
}