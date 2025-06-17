package com.vserdiuk.casestudy.repository;

import com.vserdiuk.casestudy.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link Order} entities.
 * Extends {@link JpaRepository} to provide standard CRUD operations for the {@link Order} entity.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
