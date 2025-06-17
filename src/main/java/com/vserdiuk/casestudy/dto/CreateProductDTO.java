package com.vserdiuk.casestudy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating a new Product.
 * <p>
 * This DTO encapsulates the necessary details for product creation,
 * including name, price, and available stock quantity.
 * </p>
 */
@Data
public class CreateProductDTO {
    
    /**
     * The name of the Product.
     * <p>
     * This field must not be blank.
     * </p>
     */
    @NotBlank
    private String name;

    /**
     * The price of the Product.
     * <p>
     * This field must not be null and must be greater than or equal to zero.
     * </p>
     */
    @NotNull
    @Min(0)
    private BigDecimal price;

    /**
     * The number of product units available in stock.
     * <p>
     * This field must not be null and must be zero or a positive integer.
     * </p>
     */
    @NotNull
    @Min(0)
    private Integer stockQuantity;
}
