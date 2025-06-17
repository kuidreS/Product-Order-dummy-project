package com.vserdiuk.casestudy.repository;

import com.vserdiuk.casestudy.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link ProductRepository} class.
 * <p>
 * This test class verifies the functionality of the {@link ProductRepository} using
 * Spring Data JPA's {@link DataJpaTest} environment. It tests various repository methods,
 * including existence checks by name, finding products by multiple names, and retrieving
 * products by IDs with optimistic locking. Each test ensures correct behavior for both
 * positive and negative scenarios, including edge cases like empty input collections.
 * </p>
 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Tests that {@link ProductRepository#existsByName(String)} returns {@code true}
     * when a product with the given name exists in the database.
     * <p>
     * Creates and saves a {@link Product} entity, then checks if the repository
     * correctly identifies its existence by name.
     * </p>
     */
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

    /**
     * Tests that {@link ProductRepository#existsByName(String)} returns {@code false}
     * when no product with the given name exists in the database.
     * <p>
     * Queries the repository for a non-existent product name and verifies that
     * the method returns {@code false}.
     * </p>
     */
    @Test
    @DisplayName("Should return false if product with given name does not exist")
    void shouldReturnFalseIfProductDoesNotExistByName() {
        // Act
        boolean exists = productRepository.existsByName("Non Existing Product");

        // Assert
        assertThat(exists).isFalse();
    }

    /**
     * Tests that {@link ProductRepository#findByNameIn(List)} returns all products
     * whose names match the provided list when they exist in the database.
     * <p>
     * Saves two {@link Product} entities and queries the repository with their names,
     * verifying that both products are returned with correct names.
     * </p>
     */
    @Test
    @DisplayName("Should find products by multiple names when they exist")
    void shouldFindProductsByMultipleNames() {
        // Arrange
        Product product1 = Product.builder()
                .name("Product 1")
                .price(BigDecimal.valueOf(20.0))
                .stockQuantity(50)
                .build();
        Product product2 = Product.builder()
                .name("Product 2")
                .price(BigDecimal.valueOf(30.0))
                .stockQuantity(75)
                .build();
        productRepository.saveAll(List.of(product1, product2));

        // Act
        List<Product> foundProducts = productRepository.findByNameIn(List.of("Product 1", "Product 2"));

        // Assert
        assertThat(foundProducts).hasSize(2);
        assertThat(foundProducts).extracting(Product::getName).containsExactlyInAnyOrder("Product 1", "Product 2");
    }

    /**
     * Tests that {@link ProductRepository#findByNameIn(List)} returns an empty list
     * when no products match the provided names.
     * <p>
     * Queries the repository with non-existent product names and verifies that
     * an empty list is returned.
     * </p>
     */
    @Test
    @DisplayName("Should return empty list when no products match given names")
    void shouldReturnEmptyListWhenNoProductsMatchNames() {
        // Act
        List<Product> foundProducts = productRepository.findByNameIn(List.of("Non Existing 1", "Non Existing 2"));

        // Assert
        assertThat(foundProducts).isEmpty();
    }

    /**
     * Tests that {@link ProductRepository#findByNameIn(List)} returns an empty list
     * when the input list of names is empty.
     * <p>
     * Saves a {@link Product} entity but queries the repository with an empty name list,
     * verifying that an empty list is returned.
     * </p>
     */
    @Test
    @DisplayName("Should return empty list when searching with empty name list")
    void shouldReturnEmptyListWhenNameListIsEmpty() {
        // Arrange
        Product product = Product.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();
        productRepository.save(product);

        // Act
        List<Product> foundProducts = productRepository.findByNameIn(Collections.emptyList());

        // Assert
        assertThat(foundProducts).isEmpty();
    }

    /**
     * Tests that {@link ProductRepository#findAllByIdWithLock(Set)} returns all products
     * whose IDs match the provided set, using optimistic locking.
     * <p>
     * Saves two {@link Product} entities and queries the repository with their IDs,
     * verifying that both products are returned with correct IDs.
     * </p>
     */
    @Test
    @DisplayName("Should find products by IDs with optimistic lock")
    void shouldFindProductsByIdsWithLock() {
        // Arrange
        Product product1 = Product.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(15.0))
                .stockQuantity(25)
                .build();
        Product product2 = Product.builder()
                .name("Product B")
                .price(BigDecimal.valueOf(25.0))
                .stockQuantity(30)
                .build();
        productRepository.saveAll(List.of(product1, product2));

        // Act
        List<Product> foundProducts = productRepository.findAllByIdWithLock(Set.of(product1.getId(), product2.getId()));

        // Assert
        assertThat(foundProducts).hasSize(2);
        assertThat(foundProducts).extracting(Product::getId).containsExactlyInAnyOrder(product1.getId(), product2.getId());
    }

    /**
     * Tests that {@link ProductRepository#findAllByIdWithLock(Set)} returns an empty list
     * when no products match the provided IDs.
     * <p>
     * Queries the repository with non-existent product IDs and verifies that
     * an empty list is returned.
     * </p>
     */
    @Test
    @DisplayName("Should return empty list when no products match given IDs with lock")
    void shouldReturnEmptyListWhenNoProductsMatchIdsWithLock() {
        // Act
        List<Product> foundProducts = productRepository.findAllByIdWithLock(Set.of(999L, 1000L));

        // Assert
        assertThat(foundProducts).isEmpty();
    }

    /**
     * Tests that {@link ProductRepository#findAllByIdWithLock(Set)} returns an empty list
     * when the input set of IDs is empty.
     * <p>
     * Saves a {@link Product} entity but queries the repository with an empty ID set,
     * verifying that an empty list is returned.
     * </p>
     */
    @Test
    @DisplayName("Should return empty list when searching with empty ID set with lock")
    void shouldReturnEmptyListWhenIdSetIsEmptyWithLock() {
        // Arrange
        Product product = Product.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();
        productRepository.save(product);

        // Act
        List<Product> foundProducts = productRepository.findAllByIdWithLock(Collections.emptySet());

        // Assert
        assertThat(foundProducts).isEmpty();
    }
}