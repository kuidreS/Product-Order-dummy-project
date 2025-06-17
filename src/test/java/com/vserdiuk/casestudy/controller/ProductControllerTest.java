package com.vserdiuk.casestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.dto.UpdateProductDTO;
import com.vserdiuk.casestudy.service.ProductService;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link ProductController}.
 * <p>
 * This test class verifies the behavior of the ProductController endpoints using MockMvc and Mockito.
 * It tests the creation, update, deletion, and listing of products, ensuring proper HTTP responses and
 * REST documentation generation.
 * </p>
 */
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(ProductController.class)
@AutoConfigureRestDocs
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private ProductService productServiceMock;

    /**
     * Sets up the MockMvc instance with the {@link ProductController} and configures
     * Spring REST Docs for generating documentation.
     *
     * @param restDocumentation the REST documentation context provider
     */
    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(productServiceMock))
                .setValidator(validator)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    /**
     * Tests the creation of a product via the POST /api/products endpoint.
     * Verifies that the endpoint returns a 201 Created status and the correct product details.
     * Generates REST documentation for the endpoint.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    void createProduct_shouldDocument() throws Exception {
        // Arrange
        var request = new CreateProductDTO();
        request.setName("Test Product");
        request.setPrice(BigDecimal.valueOf(10.0));
        request.setStockQuantity(100);

        var response = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();

        when(productServiceMock.createProduct(any(CreateProductDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(10.0))
                .andExpect(jsonPath("$.stockQuantity").value(100))
                .andDo(document("products/create"));
    }

    /**
     * Tests the creation of multiple products via the POST /api/products/batch endpoint.
     * Verifies that the endpoint returns a 201 Created status and the correct list of created products.
     * Generates REST documentation for the endpoint.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    void createProductsBatch_shouldDocument() throws Exception {
        // Arrange
        var request1 = new CreateProductDTO();
        request1.setName("Product 1");
        request1.setPrice(BigDecimal.valueOf(10.0));
        request1.setStockQuantity(100);

        var request2 = new CreateProductDTO();
        request2.setName("Product 2");
        request2.setPrice(BigDecimal.valueOf(20.0));
        request2.setStockQuantity(200);

        var response1 = ProductDTO.builder()
                .id(1L)
                .name("Product 1")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();

        var response2 = ProductDTO.builder()
                .id(2L)
                .name("Product 2")
                .price(BigDecimal.valueOf(20.0))
                .stockQuantity(200)
                .build();

        when(productServiceMock.createProducts(any(List.class))).thenReturn(List.of(response1, response2));

        // Act & Assert
        mockMvc.perform(post("/api/products/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request1, request2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[0].price").value(10.0))
                .andExpect(jsonPath("$[0].stockQuantity").value(100))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Product 2"))
                .andExpect(jsonPath("$[1].price").value(20.0))
                .andExpect(jsonPath("$[1].stockQuantity").value(200))
                .andDo(document("products/create-batch"));
    }

    /**
     * Tests the update of a product via the PUT /api/products endpoint.
     * Verifies that the endpoint returns a 200 OK status and the updated product details.
     * Generates REST documentation for the endpoint.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    void updateProduct_shouldDocument() throws Exception {
        // Arrange
        var request = new UpdateProductDTO();
        request.setId(1L);
        request.setName("Updated Product");
        request.setPrice(BigDecimal.valueOf(20.0));
        request.setStockQuantity(50);

        var response = ProductDTO.builder()
                .id(1L)
                .name("Updated Product")
                .price(BigDecimal.valueOf(20.0))
                .stockQuantity(50)
                .build();

        when(productServiceMock.updateProduct(any(UpdateProductDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(20.0))
                .andExpect(jsonPath("$.stockQuantity").value(50))
                .andDo(document("products/update"));
    }

    /**
     * Tests the update of multiple products via the PUT /api/products/batch endpoint.
     * Verifies that the endpoint returns a 200 OK status and the updated list of products.
     * Generates REST documentation for the endpoint.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    void updateProductsBatch_shouldDocument() throws Exception {
        // Arrange
        var request1 = new UpdateProductDTO();
        request1.setId(1L);
        request1.setName("Updated Product 1");
        request1.setPrice(BigDecimal.valueOf(15.0));
        request1.setStockQuantity(150);

        var request2 = new UpdateProductDTO();
        request2.setId(2L);
        request2.setName("Updated Product 2");
        request2.setPrice(BigDecimal.valueOf(25.0));
        request2.setStockQuantity(250);

        var response1 = ProductDTO.builder()
                .id(1L)
                .name("Updated Product 1")
                .price(BigDecimal.valueOf(15.0))
                .stockQuantity(150)
                .build();

        var response2 = ProductDTO.builder()
                .id(2L)
                .name("Updated Product 2")
                .price(BigDecimal.valueOf(25.0))
                .stockQuantity(250)
                .build();

        when(productServiceMock.updateProducts(any(List.class))).thenReturn(List.of(response1, response2));

        // Act & Assert
        mockMvc.perform(put("/api/products/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request1, request2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Updated Product 1"))
                .andExpect(jsonPath("$[0].price").value(15.0))
                .andExpect(jsonPath("$[0].stockQuantity").value(150))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Updated Product 2"))
                .andExpect(jsonPath("$[1].price").value(25.0))
                .andExpect(jsonPath("$[1].stockQuantity").value(250))
                .andDo(document("products/update-batch"));
    }

    /**
     * Tests the deletion of a product via the DELETE /api/products/{id} endpoint.
     * Verifies that the endpoint returns a 204 No Content status.
     * Generates REST documentation for the endpoint.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    void deleteProduct_shouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isNoContent())
                .andDo(document("products/delete"));
    }

    /**
     * Tests the deletion of multiple products via the DELETE /api/products endpoint.
     * Verifies that the endpoint returns a 204 No Content status.
     * Generates REST documentation for the endpoint.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    void deleteProductsBatch_shouldReturnNoContent() throws Exception {
        // Arrange
        List<Long> ids = List.of(1L, 2L);

        // Act & Assert
        mockMvc.perform(delete("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isNoContent())
                .andDo(document("products/delete-batch"));
    }

    /**
     * Tests the retrieval of a product by ID via the GET /api/products/{id} endpoint.
     * Verifies that the endpoint returns a 200 OK status and the correct product details.
     * Generates REST documentation for the endpoint.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    void getProduct_shouldReturnProduct() throws Exception {
        // Arrange
        var response = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(100)
                .build();

        when(productServiceMock.getProduct(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(10.0))
                .andExpect(jsonPath("$.stockQuantity").value(100))
                .andDo(document("products/get"));
    }

    /**
     * Tests the retrieval of a paginated list of products via the GET /api/products endpoint.
     * Verifies that the endpoint returns a 200 OK status and the correct product list.
     * Generates REST documentation for the endpoint.
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    void listProducts_shouldReturnProductList() throws Exception {
        // Arrange
        var product1 = ProductDTO.builder().id(1L).name("Product 1").price(BigDecimal.valueOf(10.0)).stockQuantity(100).build();
        var product2 = ProductDTO.builder().id(2L).name("Product 2").price(BigDecimal.valueOf(20.0)).stockQuantity(200).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(productServiceMock.getAllProducts(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product1, product2), pageable, 2));

        // Act & Assert
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Product 1"))
                .andExpect(jsonPath("$.content[0].price").value(10.0))
                .andExpect(jsonPath("$.content[0].stockQuantity").value(100))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].name").value("Product 2"))
                .andExpect(jsonPath("$.content[1].price").value(20.0))
                .andExpect(jsonPath("$.content[1].stockQuantity").value(200))
                .andDo(document("products/list"));
    }

    /**
     * Test configuration to provide a mocked {@link ProductService} bean.
     */
    @TestConfiguration
    static class MockConfig {
        /**
         * Creates a mocked instance of {@link ProductService}.
         *
         * @return a mocked ProductService
         */
        @Bean
        public ProductService productService() {
            return mock(ProductService.class);
        }
    }
}