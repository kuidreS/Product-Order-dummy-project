package com.vserdiuk.casestudy.validator;

import com.vserdiuk.casestudy.entity.Order;
import com.vserdiuk.casestudy.entity.OrderStatus;
import com.vserdiuk.casestudy.exception.BusinessException;
import com.vserdiuk.casestudy.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Component;

/**
 * A validator component for enforcing order-related business rules in the order management system.
 * <p>
 * This class provides methods to validate the state of orders, ensuring that operations such as cancellation,
 * payment, or expiration are only performed on orders with valid statuses. It uses optimistic locking to
 * maintain data consistency during validation.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class OrderValidator {

    private final OrderRepository orderRepository;

    /**
     * Validates that an order with the specified ID is in the CREATED status and returns it with an optimistic lock.
     * <p>
     * This method retrieves the order from the repository using the provided ID and checks if its status is
     * {@link OrderStatus#CREATED}. If the order is not found or is not in the CREATED status, an exception is thrown.
     * The method applies optimistic locking to ensure data consistency during validation.
     * </p>
     *
     * @param orderId The ID of the order to validate.
     * @param action  The action being performed (e.g., "canceled", "paid") for inclusion in the exception message.
     * @return The locked {@link Order} entity if validation passes.
     * @throws EntityNotFoundException if no order is found with the specified ID.
     * @throws BusinessException       if the order's status is not {@link OrderStatus#CREATED}.
     */
    @Lock(LockModeType.OPTIMISTIC)
    public Order validateOrderIsCreated(Long orderId, String action) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException(String.format("Only CREATED orders can be %s", action));
        }
        return order;
    }

    /**
     * Validates that an order with the specified ID is in the CREATED status for expiration purposes.
     * <p>
     * This method retrieves the order from the repository using the provided ID and checks if its status is
     * {@link OrderStatus#CREATED}. If the order is not in the CREATED status, it returns null to indicate that
     * the order is not eligible for expiration. Optimistic locking is applied to ensure data consistency.
     * </p>
     *
     * @param orderId The ID of the order to validate.
     * @return The locked {@link Order} entity if the order is in CREATED status, or null otherwise.
     * @throws EntityNotFoundException if no order is found with the specified ID.
     */
    @Lock(LockModeType.OPTIMISTIC)
    public Order validateOrderForExpiration(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));
        return order.getStatus() == OrderStatus.CREATED ? order : null;
    }
}