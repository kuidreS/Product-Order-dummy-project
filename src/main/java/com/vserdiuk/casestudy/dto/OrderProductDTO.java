package com.vserdiuk.casestudy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data Transfer Object representing a Product in an Order.
 * <p>
 * This DTO is used to specify which Product is being ordered and in what quantity.
 * </p>
 */
@Data
public class OrderProductDTO {

    /**
     * The unique identifier of the Product being ordered.
     * <p>
     * This field must not be {@code null}.
     * </p>
     */
    @NotNull
    private Long productId;

    /**
     * The quantity of the Product being ordered.
     * <p>
     * This field must not be {@code null} and must be at least 1.
     * </p>
     */
    @NotNull
    @Min(1)
    private Integer quantity;
}
