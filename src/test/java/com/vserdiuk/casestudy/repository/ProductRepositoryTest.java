package com.vserdiuk.casestudy.repository;

import com.vserdiuk.casestudy.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    // Positive scenario
    @Test
    @DisplayName("Should return true if product with given name exists")
    void shouldReturnTrueIfProductExistsByName() {
        // Arrange
        Product product = Product.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();
        productRepository.save(product);

        // Act
        boolean exists = productRepository.existsByName("Test Product");

        // Assert
        assertThat(exists).isTrue();
    }

    // Negative scenario
    @Test
    @DisplayName("Should return false if product with given name does not exist")
    void shouldReturnFalseIfProductDoesNotExistByName() {
        // Act
        boolean exists = productRepository.existsByName("Non Existing Product");

        // Assert
        assertThat(exists).isFalse();
    }
}