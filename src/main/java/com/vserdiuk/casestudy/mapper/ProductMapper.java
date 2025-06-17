package com.vserdiuk.casestudy.mapper;

import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.entity.Product;

/**
 * Utility class for mapping between {@link Product} entities and DTOs.
 */
public class ProductMapper {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private ProductMapper() {
    }

    /**
     * Converts a {@link Product} entity to a {@link ProductDTO}.
     * <p>
     * This method maps the properties of a {@link Product} entity (id, name, price,
     * and stock quantity) to a {@link ProductDTO} using the builder pattern. If the
     * input {@code product} is {@code null}, the method returns {@code null}.
     * </p>
     *
     * @param product the {@link Product} entity to be converted
     * @return a {@link ProductDTO} containing the mapped properties, or {@code null}
     * if the input is {@code null}
     */
    public static ProductDTO toDTO(Product product) {
        if (product == null) return null;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }

    /**
     * Converts a {@link CreateProductDTO} to a {@link Product} entity.
     * <p>
     * This method maps the properties of a {@link CreateProductDTO} (name, price,
     * and stock quantity) to a {@link Product} entity using the builder pattern. If
     * the input {@code dto} is {@code null}, the method returns {@code null}.
     * </p>
     *
     * @param dto the {@link CreateProductDTO} to be converted
     * @return a {@link Product} entity containing the mapped properties, or
     * {@code null} if the input is {@code null}
     */
    public static Product toEntity(CreateProductDTO dto) {
        if (dto == null) return null;

        return Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .build();
    }
}
