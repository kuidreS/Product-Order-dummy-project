package com.vserdiuk.casestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.dto.UpdateProductDTO;
import com.vserdiuk.casestudy.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(productServiceMock))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

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

    @Test
    void deleteProduct_shouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isNoContent())
                .andDo(document("products/delete"));
    }

    @Test
    void listProducts_shouldReturnProductList() throws Exception {
        // Arrange
        var product1 = ProductDTO.builder().id(1L).name("Product 1").price(BigDecimal.valueOf(10.0)).stockQuantity(100).build();
        var product2 = ProductDTO.builder().id(2L).name("Product 2").price(BigDecimal.valueOf(20.0)).stockQuantity(200).build();

        when(productServiceMock.listProducts()).thenReturn(List.of(product1, product2));

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[0].price").value(10.0))
                .andExpect(jsonPath("$[0].stockQuantity").value(100))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Product 2"))
                .andExpect(jsonPath("$[1].price").value(20.0))
                .andExpect(jsonPath("$[1].stockQuantity").value(200))
                .andDo(document("products/list"));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ProductService productService() {
            return mock(ProductService.class);
        }
    }

}