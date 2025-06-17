package com.vserdiuk.casestudy.controller;

import com.vserdiuk.casestudy.dto.CreateOrderDTO;
import com.vserdiuk.casestudy.dto.OrderDTO;
import com.vserdiuk.casestudy.exception.BusinessException;
import com.vserdiuk.casestudy.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Order-related operations.
 * <p>
 * This controller provides endpoints for creating, paying, and canceling orders.
 * All endpoints are prefixed with "/api/orders".
 * </p>
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order API", description = "Operations related to order management")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order with the specified products and quantities.
     *
     * @param dto the data transfer object containing the details required to create an order
     * @return a {@link ResponseEntity} containing the created {@link OrderDTO} with HTTP status 201 (Created)
     */
    @Operation(summary = "Create a new order with products and quantities")
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        return new ResponseEntity<>(orderService.createOrder(dto), HttpStatus.CREATED);
    }

    /**
     * Marks an existing order as paid.
     *
     * @param id the ID of the order to mark as paid
     * @return a {@link ResponseEntity} with HTTP status 200 (OK) indicating successful payment
     */
    @Operation(summary = "Mark an order as paid")
    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> payOrder(@PathVariable Long id) {
        orderService.payOrder(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancels an existing order and releases associated stock.
     *
     * @param id the ID of the order to cancel
     * @return a {@link ResponseEntity} with HTTP status 200 (OK) indicating successful cancellation
     */
    @Operation(summary = "Cancel an existing order and release stock")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles BusinessException and returns a 400 Bad Request response with the error message.
     *
     * @param ex the BusinessException thrown during request processing
     * @return a {@link ResponseEntity} with HTTP status 400 (Bad Request) and the exception message
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}