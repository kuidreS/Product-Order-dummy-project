package com.vserdiuk.casestudy.repository;

import com.vserdiuk.casestudy.entity.Order;
import com.vserdiuk.casestudy.entity.OrderExpirationTask;
import com.vserdiuk.casestudy.entity.OrderStatus;
import com.vserdiuk.casestudy.entity.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link OrderExpirationTaskRepository} class.
 * <p>
 * This test class verifies the functionality of the {@link OrderExpirationTaskRepository} using
 * Spring Data JPA's {@link DataJpaTest} environment. It focuses on testing the repository's custom
 * query method {@code findByStatusAndExpirationTimeBefore}, ensuring it correctly retrieves
 * {@link OrderExpirationTask} entities based on their status and expiration time.
 * </p>
 */
@DataJpaTest
class OrderExpirationTaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderExpirationTaskRepository repository;

    private LocalDateTime now;

    /**
     * Sets up the test environment before each test case.
     * <p>
     * Initializes the current timestamp and clears the database by deleting all
     * {@link OrderExpirationTask} entities to ensure a clean state for each test.
     * </p>
     */
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        repository.deleteAll();
    }

    /**
     * Tests the {@code findByStatusAndExpirationTimeBefore} method to ensure it returns tasks
     * matching the specified status and expiration time criteria.
     * <p>
     * This test creates multiple {@link Order} and {@link OrderExpirationTask} entities with
     * varying expiration times and statuses. It verifies that only tasks with {@link TaskStatus#PENDING}
     * and an expiration time before the provided timestamp are returned.
     * </p>
     */
    @Test
    void findByStatusAndExpirationTimeBefore_shouldReturnMatchingTasks() {
        // Arrange
        Order order1 = Order.builder()
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .build();
        Order order2 = Order.builder()
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .build();
        Order order3 = Order.builder()
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .build();
        Order order4 = Order.builder()
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .build();

        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.persist(order3);
        entityManager.persist(order4);
        entityManager.flush();

        OrderExpirationTask task1 = OrderExpirationTask.builder()
                .orderId(order1.getId())
                .expirationTime(now.minusHours(1))
                .status(TaskStatus.PENDING)
                .build();

        OrderExpirationTask task2 = OrderExpirationTask.builder()
                .orderId(order2.getId())
                .expirationTime(now.minusHours(2))
                .status(TaskStatus.PENDING)
                .build();

        OrderExpirationTask task3 = OrderExpirationTask.builder()
                .orderId(order3.getId())
                .expirationTime(now.plusHours(1))
                .status(TaskStatus.PENDING)
                .build();

        OrderExpirationTask task4 = OrderExpirationTask.builder()
                .orderId(order4.getId())
                .expirationTime(now.minusHours(1))
                .status(TaskStatus.SENT)
                .build();

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.persist(task4);
        entityManager.flush();

        // Act
        List<OrderExpirationTask> result = repository.findByStatusAndExpirationTimeBefore(
                TaskStatus.PENDING, now);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderExpirationTask::getOrderId)
                .containsExactlyInAnyOrder(order1.getId(), order2.getId());
        assertThat(result).extracting(OrderExpirationTask::getStatus)
                .containsOnly(TaskStatus.PENDING);
    }

    /**
     * Tests the {@code findByStatusAndExpirationTimeBefore} method when no tasks match the criteria.
     * <p>
     * This test creates an {@link Order} and an {@link OrderExpirationTask} with an expiration time
     * in the future. It verifies that an empty list is returned when querying for tasks with
     * {@link TaskStatus#PENDING} and an expiration time before a past timestamp.
     * </p>
     */
    @Test
    void findByStatusAndExpirationTimeBefore_shouldReturnEmptyListWhenNoMatches() {
        // Arrange
        Order order = Order.builder()
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .build();
        entityManager.persist(order);
        entityManager.flush();

        OrderExpirationTask task = OrderExpirationTask.builder()
                .orderId(order.getId())
                .expirationTime(now.plusHours(1))
                .status(TaskStatus.PENDING)
                .build();

        entityManager.persist(task);
        entityManager.flush();

        // Act
        List<OrderExpirationTask> result = repository.findByStatusAndExpirationTimeBefore(
                TaskStatus.PENDING, now.minusHours(1));

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * Tests the {@code findByStatusAndExpirationTimeBefore} method when no tasks match the status criteria.
     * <p>
     * This test creates an {@link Order} and an {@link OrderExpirationTask} with a matching expiration
     * time but a non-matching status ({@link TaskStatus#SENT}). It verifies that an empty list is
     * returned when querying for tasks with {@link TaskStatus#PENDING}.
     * </p>
     */
    @Test
    void findByStatusAndExpirationTimeBefore_shouldReturnEmptyListWhenStatusDoesNotMatch() {
        // Arrange
        Order order = Order.builder()
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .build();
        entityManager.persist(order);
        entityManager.flush();

        OrderExpirationTask task = OrderExpirationTask.builder()
                .orderId(order.getId())
                .expirationTime(now.minusHours(1))
                .status(TaskStatus.SENT)
                .build();

        entityManager.persist(task);
        entityManager.flush();

        // Act
        List<OrderExpirationTask> result = repository.findByStatusAndExpirationTimeBefore(
                TaskStatus.PENDING, now);

        // Assert
        assertThat(result).isEmpty();
    }
}