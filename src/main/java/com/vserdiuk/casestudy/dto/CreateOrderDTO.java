package com.vserdiuk.casestudy.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object for creating a new Order.
 * <p>
 * This DTO is used to encapsulate the data required to create an order,
 * including a list of order product items.
 * </p>
 */
@Data
public class CreateOrderDTO {

    /**
     * A list of products included in the Order.
     * <p>
     * This list must not be empty. Each item in the list represents
     * a product that is being ordered.
     * </p>
     */
    @NotEmpty
    private List<OrderProductDTO> items;
}
