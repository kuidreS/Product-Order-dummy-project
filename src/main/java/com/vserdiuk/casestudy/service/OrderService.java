package com.vserdiuk.casestudy.service;

import com.vserdiuk.casestudy.dto.CreateOrderDTO;
import com.vserdiuk.casestudy.dto.OrderDTO;

/**
 * Service interface for managing Orders in the system.
 * <p>
 * Provides operations for creating, canceling, paying, and expiring Orders.
 * When an order is created, the associated products are blocked for 30 minutes.
 * If the order is not paid within 30 minutes, it will be removed from the system.
 * </p>
 */
public interface OrderService {

    /**
     * Creates a new order based on the provided data.
     * <p>
     * The products associated with the order are blocked for 30 minutes upon creation.
     * If the order is not paid within this period, it will be removed.
     * Throws {@link com.vserdiuk.casestudy.exception.BusinessException} if there are some Products in stock issues.
     * </p>
     *
     * @param dto the data transfer object containing order creation details
     * @return the created order as an {@link OrderDTO}
     */
    OrderDTO createOrder(CreateOrderDTO dto);

    /**
     * Cancels an existing order by its ID.
     * Throws {@link jakarta.persistence.EntityNotFoundException} if there is not such Order with ID in the system.
     * Throws {@link com.vserdiuk.casestudy.exception.BusinessException} if there is an attempt to cancel an order that is not in the CREATED status.
     *
     * @param orderId the ID of the order to cancel
     */
    void cancelOrder(Long orderId);

    /**
     * Marks an order as paid by its ID.
     * <p>
     * This action must occur within 30 minutes of order creation to prevent expiration.
     * Throws {@link jakarta.persistence.EntityNotFoundException} if there is not such Order with ID in the system.
     * Throws {@link com.vserdiuk.casestudy.exception.BusinessException} if there is an attempt to pay an Order that is not in the CREATED status.
     * </p>
     *
     * @param orderId the ID of the order to mark as paid
     */
    void payOrder(Long orderId);

    /**
     * Expires an order by its ID, removing it from the system.
     * <p>
     * Typically used when an order is not paid within the 30-minute window.
     * Throws {@link jakarta.persistence.EntityNotFoundException} if there is not such Order with ID in the system.
     * </p>
     *
     * @param orderId the ID of the order to expire
     */
    void expireOrderById(Long orderId);
}
