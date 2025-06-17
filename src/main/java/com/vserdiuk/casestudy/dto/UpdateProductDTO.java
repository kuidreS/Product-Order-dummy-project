package com.vserdiuk.casestudy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Data Transfer Object for updating an existing Product.
 * <p>
 * This DTO allows partial or full updates to a product's attributes,
 * such as its name, price, and stock quantity.
 * </p>
 */
@Data
public class UpdateProductDTO {

    /**
     * The unique identifier of the product to be updated.
     * <p>
     * This field is mandatory and must not be {@code null}.
     * </p>
     */
    @NotNull
    private Long id;

    /**
     * The new name of the Product.
     * <p>
     * This field is optional; if {@code null}, the name will remain unchanged.
     * </p>
     */
    private String name;

    /**
     * The new price of the Product.
     * <p>
     * Must be greater than or equal to 0 if provided.
     * </p>
     */
    @Min(0)
    private BigDecimal price;

    /**
     * The new stock quantity of the Product.
     * <p>
     * Must be zero or greater if provided.
     * </p>
     */
    @Min(0)
    private Integer stockQuantity;
}

