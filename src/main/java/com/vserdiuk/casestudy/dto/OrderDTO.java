package com.vserdiuk.casestudy.dto;

import com.vserdiuk.casestudy.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object representing an Order.
 * <p>
 * This DTO is used for transferring order data, including its ID, status,
 * timestamps, and associated products.
 * </p>
 */
@Data
@Builder
public class OrderDTO {

    /**
     * The unique identifier of the Order.
     */
    private Long id;

    /**
     * The current status of the Order.
     * <p>
     * Represents the state of the order (e.g., CREATED, PAID, SHIPPED).
     * </p>
     */
    private OrderStatus status;

    /**
     * The timestamp when the order was created.
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when the order was paid.
     * <p>
     * May be {@code null} if the order has not been paid yet.
     * </p>
     */
    private LocalDateTime paidAt;

    /**
     * The list of products associated with this Order.
     * <p>
     * Each item represents a product included in the Order.
     * </p>
     */
    private List<ProductDTO> products;
}
