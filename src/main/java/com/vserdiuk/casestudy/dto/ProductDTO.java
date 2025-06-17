package com.vserdiuk.casestudy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Data Transfer Object representing a Product.
 * <p>
 * This DTO is used for transferring product data such as its ID, name, price,
 * and available stock quantity.
 * </p>
 */
@Data
@Builder
public class ProductDTO {

    /**
     * The unique identifier of the Product.
     */
    private Long id;

    /**
     * The name of the Product.
     */
    private String name;

    /**
     * The price of the Product.
     */
    private BigDecimal price;

    /**
     * The number of units of the product available in stock.
     */
    private int stockQuantity;
}
