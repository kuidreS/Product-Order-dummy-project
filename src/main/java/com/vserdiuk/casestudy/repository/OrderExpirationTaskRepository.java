package com.vserdiuk.casestudy.repository;

import com.vserdiuk.casestudy.entity.OrderExpirationTask;
import com.vserdiuk.casestudy.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing {@link OrderExpirationTask} entities.
 * Extends {@link JpaRepository} to provide standard CRUD operations and additional query methods for the {@link OrderExpirationTask} entity.
 */
public interface OrderExpirationTaskRepository extends JpaRepository<OrderExpirationTask, Long> {

    /**
     * Retrieves a list of {@link OrderExpirationTask} entities with the specified status and expiration time before the given time.
     *
     * @param status         the status of the order expiration tasks to search for
     * @param expirationTime the cutoff time to filter tasks whose expiration time is before this value
     * @return a list of {@link OrderExpirationTask} entities matching the specified status and expiration time criteria
     */
    List<OrderExpirationTask> findByStatusAndExpirationTimeBefore(TaskStatus status, LocalDateTime expirationTime);
}