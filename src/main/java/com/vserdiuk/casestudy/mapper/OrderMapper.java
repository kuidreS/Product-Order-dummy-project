package com.vserdiuk.casestudy.mapper;

import com.vserdiuk.casestudy.dto.OrderDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.entity.Order;
import com.vserdiuk.casestudy.entity.OrderProduct;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between {@link Order} entities and DTOs.
 */
public class OrderMapper {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private OrderMapper() {
    }

    /**
     * Converts an {@link Order} entity to an {@link OrderDTO}.
     * <p>
     * This method maps the properties of an {@link Order} entity (id, status, createdAt,
     * paidAt, and associated products) to an {@link OrderDTO} using the builder pattern.
     * The associated products are extracted from the {@link OrderProduct} list, converted
     * to {@link ProductDTO} using {@link ProductMapper#toDTO}, and collected into a list.
     * If the input {@code order} is {@code null}, the method returns {@code null}.
     * </p>
     *
     * @param order the {@link Order} entity to be converted
     * @return an {@link OrderDTO} containing the mapped properties, or {@code null}
     *         if the input is {@code null}
     */
    public static OrderDTO toDTO(Order order) {
        if (order == null) return null;

        List<ProductDTO> products = order.getOrderProducts().stream()
                .map(OrderProduct::getProduct)
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .products(products)
                .build();
    }
}