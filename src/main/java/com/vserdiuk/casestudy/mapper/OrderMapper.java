package com.vserdiuk.casestudy.mapper;

import com.vserdiuk.casestudy.dto.OrderDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.entity.Order;
import com.vserdiuk.casestudy.entity.OrderProduct;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {

    }

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
