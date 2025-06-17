package com.vserdiuk.casestudy.repository;

import com.vserdiuk.casestudy.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link OrderProduct} entities.
 * Extends {@link JpaRepository} to provide standard CRUD operations for the {@link OrderProduct} entity.
 */
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}
