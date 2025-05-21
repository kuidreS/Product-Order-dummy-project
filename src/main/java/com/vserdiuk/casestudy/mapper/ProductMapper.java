package com.vserdiuk.casestudy.mapper;

import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.entity.Product;

public class ProductMapper {

    private ProductMapper() {
    }

    public static ProductDTO toDTO(Product product) {
        if (product == null) return null;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
