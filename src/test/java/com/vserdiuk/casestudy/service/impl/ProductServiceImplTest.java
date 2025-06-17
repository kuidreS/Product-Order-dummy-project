/**
 * Unit tests for the {@link ProductServiceImpl} class, which handles product-related operations
 * such as creation, updating, deletion, and retrieval of products.
 */
package com.vserdiuk.casestudy.service.impl;

import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.dto.UpdateProductDTO;
import com.vserdiuk.casestudy.entity.Product;
import com.vserdiuk.casestudy.repository.ProductRepository;
import com.vserdiuk.casestudy.validator.ProductValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link ProductServiceImpl} using Mockito for mocking dependencies and AssertJ for assertions.
 */
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductValidator productValidator;

    @InjectMocks
    private ProductServiceImpl productService;

    /**
     * Initializes mocks before each test method.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests successful creation of a single product, verifying the saved product's details.
     */
    @Test
    void shouldCreateProductSuccessfully() {
        // Arrange
        CreateProductDTO dto = new CreateProductDTO();
        dto.setName("Test Product");
        dto.setPrice(BigDecimal.valueOf(10.0));
        dto.setStockQuantity(100);

        Product product = Product.builder()
                .id(1L)
                .name(dto.getName())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .build();

        when(productRepository.existsByName(dto.getName())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        ProductDTO result = productService.createProduct(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(dto.getName());
        assertThat(result.getPrice()).isEqualTo(dto.getPrice());
        assertThat(result.getStockQuantity()).isEqualTo(dto.getStockQuantity());
        verify(productValidator).validateDTO(dto);
        verify(productRepository).save(any(Product.class));
    }

    /**
     * Tests failure to create a product when the product name already exists.
     */
    @Test
    void shouldThrowIllegalArgumentExceptionWhenProductNameExists() {
        // Arrange
        CreateProductDTO dto = new CreateProductDTO();
        dto.setName("Existing Product");
        dto.setPrice(BigDecimal.valueOf(10.0));
        dto.setStockQuantity(100);

        when(productRepository.existsByName(dto.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product with name " + dto.getName() + " already exists");
        verify(productValidator).validateDTO(dto);
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Tests successful creation of multiple products, verifying the saved products' details.
     */
    @Test
    void shouldCreateMultipleProductsSuccessfully() {
        // Arrange
        CreateProductDTO dto1 = new CreateProductDTO();
        dto1.setName("Product 1");
        dto1.setPrice(BigDecimal.valueOf(10.0));
        dto1.setStockQuantity(100);

        CreateProductDTO dto2 = new CreateProductDTO();
        dto2.setName("Product 2");
        dto2.setPrice(BigDecimal.valueOf(20.0));
        dto2.setStockQuantity(200);

        List<CreateProductDTO> dtos = List.of(dto1, dto2);

        Product product1 = Product.builder()
                .id(1L)
                .name(dto1.getName())
                .price(dto1.getPrice())
                .stockQuantity(dto1.getStockQuantity())
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name(dto2.getName())
                .price(dto2.getPrice())
                .stockQuantity(dto2.getStockQuantity())
                .build();

        when(productRepository.findByNameIn(any())).thenReturn(List.of());
        when(productRepository.saveAll(any())).thenReturn(List.of(product1, product2));

        // Act
        List<ProductDTO> result = productService.createProducts(dtos);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Product 1");
        assertThat(result.get(1).getName()).isEqualTo("Product 2");
        verify(productValidator, times(2)).validateDTO(any(CreateProductDTO.class));
        verify(productRepository).saveAll(any());
    }

    /**
     * Tests failure to create multiple products with duplicate names.
     */
    @Test
    void shouldThrowIllegalArgumentExceptionWhenCreatingDuplicateProductNames() {
        // Arrange
        CreateProductDTO dto1 = new CreateProductDTO();
        dto1.setName("Product");
        CreateProductDTO dto2 = new CreateProductDTO();
        dto2.setName("Product");

        List<CreateProductDTO> dtos = List.of(dto1, dto2);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProducts(dtos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate Product names found");
        verify(productRepository, never()).saveAll(any());
    }

    /**
     * Tests successful update of a product, verifying the updated product's details.
     */
    @Test
    void shouldUpdateProductSuccessfully() {
        // Arrange
        UpdateProductDTO dto = new UpdateProductDTO();
        dto.setId(1L);
        dto.setName("Updated Name");
        dto.setPrice(BigDecimal.valueOf(20.0));
        dto.setStockQuantity(50);

        Product existingProduct = Product.builder()
                .id(1L)
                .name("Old Name")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name(dto.getName())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .build();

        when(productRepository.findById(dto.getId())).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByName(dto.getName())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ProductDTO result = productService.updateProduct(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(dto.getName());
        assertThat(result.getPrice()).isEqualTo(dto.getPrice());
        assertThat(result.getStockQuantity()).isEqualTo(dto.getStockQuantity());
        verify(productValidator).validateDTO(dto);
        verify(productRepository).save(existingProduct);
    }

    /**
     * Tests failure to update a non-existing product.
     */
    @Test
    void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistingProduct() {
        // Arrange
        UpdateProductDTO dto = new UpdateProductDTO();
        dto.setId(1L);

        when(productRepository.findById(dto.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + dto.getId());
        verify(productValidator).validateDTO(dto);
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Tests successful update of multiple products, verifying the updated products' details.
     */
    @Test
    void shouldUpdateMultipleProductsSuccessfully() {
        // Arrange
        UpdateProductDTO dto1 = new UpdateProductDTO();
        dto1.setId(1L);
        dto1.setName("Updated Product 1");
        dto1.setPrice(BigDecimal.valueOf(15.0));
        dto1.setStockQuantity(150);

        UpdateProductDTO dto2 = new UpdateProductDTO();
        dto2.setId(2L);
        dto2.setName("Updated Product 2");
        dto2.setPrice(BigDecimal.valueOf(25.0));
        dto2.setStockQuantity(250);

        List<UpdateProductDTO> dtos = List.of(dto1, dto2);

        Product product1 = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .price(BigDecimal.valueOf(20.0))
                .stockQuantity(200)
                .build();

        when(productRepository.findByNameIn(any())).thenReturn(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.saveAll(any())).thenReturn(List.of(product1, product2));

        // Act
        List<ProductDTO> result = productService.updateProducts(dtos);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Updated Product 1");
        assertThat(result.get(1).getName()).isEqualTo("Updated Product 2");
        verify(productValidator, times(2)).validateDTO(any(UpdateProductDTO.class));
        verify(productRepository).saveAll(any());
    }

    /**
     * Tests failure to update multiple products with duplicate names.
     */
    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdatingWithDuplicateNames() {
        // Arrange
        UpdateProductDTO dto1 = new UpdateProductDTO();
        dto1.setId(1L);
        dto1.setName("Product");
        UpdateProductDTO dto2 = new UpdateProductDTO();
        dto2.setId(2L);
        dto2.setName("Product");

        List<UpdateProductDTO> dtos = List.of(dto1, dto2);

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProducts(dtos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate Product names found");
        verify(productRepository, never()).saveAll(any());
    }

    /**
     * Tests successful deletion of a product by ID.
     */
    @Test
    void shouldDeleteProductSuccessfully() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).deleteById(productId);
    }

    /**
     * Tests failure to delete a non-existing product.
     */
    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistingProduct() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + productId);
        verify(productRepository, never()).deleteById(any());
    }

    /**
     * Tests successful deletion of multiple products by IDs.
     */
    @Test
    void shouldDeleteMultipleProductsSuccessfully() {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        Product product1 = Product.builder().id(1L).build();
        Product product2 = Product.builder().id(2L).build();

        when(productRepository.findAllById(ids)).thenReturn(List.of(product1, product2));

        // Act
        productService.deleteProducts(ids);

        // Assert
        verify(productRepository).deleteAllById(ids);
    }

    /**
     * Tests failure to delete multiple products with duplicate IDs.
     */
    @Test
    void shouldThrowIllegalArgumentExceptionWhenDeletingDuplicateIds() {
        // Arrange
        List<Long> ids = List.of(1L, 1L);

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProducts(ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate Product IDs found");
        verify(productRepository, never()).deleteAllById(any());
    }

    /**
     * Tests failure to delete multiple products when some IDs do not exist.
     */
    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistingProducts() {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        when(productRepository.findAllById(ids)).thenReturn(List.of(Product.builder().id(1L).build()));

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProducts(ids))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Products not found with ids: [2]");
        verify(productRepository, never()).deleteAllById(any());
    }

    /**
     * Tests successful retrieval of a product by ID.
     */
    @Test
    void shouldGetProductSuccessfully() {
        // Arrange
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        ProductDTO result = productService.getProduct(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo(product.getName());
        verify(productRepository).findById(productId);
    }

    /**
     * Tests failure to retrieve a non-existing product.
     */
    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingNonExistingProduct() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + productId);
    }

    /**
     * Tests successful retrieval of all products with pagination.
     */
    @Test
    void shouldGetAllProductsSuccessfully() {
        // Arrange
        Product product1 = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();
        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .price(BigDecimal.valueOf(20.0))
                .stockQuantity(200)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product1, product2), pageable, 2);
        when(productRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<ProductDTO> result = productService.getAllProducts(pageable);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Product 1");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Product 2");
        verify(productRepository).findAll(pageable);
    }
}