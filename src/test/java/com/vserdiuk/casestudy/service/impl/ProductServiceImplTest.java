package com.vserdiuk.casestudy.service.impl;

import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.UpdateProductDTO;
import com.vserdiuk.casestudy.entity.Product;
import com.vserdiuk.casestudy.exception.BusinessException;
import com.vserdiuk.casestudy.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateProductSuccessfully() {
        // Arrange
        var dto = new CreateProductDTO();
        dto.setName("Test Product");
        dto.setPrice(BigDecimal.valueOf(10.0));
        dto.setStockQuantity(100);

        var savedProduct = Product.builder()
                .id(1L)
                .name(dto.getName())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .build();

        when(productRepository.existsByName(dto.getName())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        var result = productService.createProduct(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenProductWithNameExists() {
        // Arrange
        var dto = new CreateProductDTO();
        dto.setName("Existing Product");
        dto.setPrice(BigDecimal.valueOf(10.0));
        dto.setStockQuantity(100);

        when(productRepository.existsByName(dto.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        // Arrange
        var dto = new UpdateProductDTO();
        dto.setId(1L);
        dto.setName("Updated Name");
        dto.setPrice(BigDecimal.valueOf(20.0));
        dto.setStockQuantity(50);

        var existingProduct = Product.builder()
                .id(1L)
                .name("Old Name")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();

        when(productRepository.findById(dto.getId())).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        var result = productService.updateProduct(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        verify(productRepository).save(existingProduct);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistingProduct() {
        // Arrange
        var dto = new UpdateProductDTO();
        dto.setId(1L);

        when(productRepository.findById(dto.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        // Arrange
        var productId = 1L;

        var existingProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).delete(existingProduct);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistingProduct() {
        // Arrange
        var productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void shouldListAllProducts() {
        // Arrange
        var product1 = Product.builder().id(1L).name("Product 1").price(BigDecimal.valueOf(10.0)).stockQuantity(100).build();
        var product2 = Product.builder().id(2L).name("Product 2").price(BigDecimal.valueOf(20.0)).stockQuantity(200).build();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        // Act
        var result = productService.listProducts();

        // Assert
        assertThat(result).hasSize(2);
        verify(productRepository).findAll();
    }

}
