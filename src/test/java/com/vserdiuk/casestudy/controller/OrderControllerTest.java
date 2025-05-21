package com.vserdiuk.casestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vserdiuk.casestudy.dto.CreateOrderDTO;
import com.vserdiuk.casestudy.dto.OrderDTO;
import com.vserdiuk.casestudy.dto.OrderProductDTO;
import com.vserdiuk.casestudy.entity.OrderStatus;
import com.vserdiuk.casestudy.exception.BusinessException;
import com.vserdiuk.casestudy.service.OrderService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(OrderController.class)
@AutoConfigureRestDocs
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private OrderService orderServiceMock;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public OrderService orderService() {
            return mock(OrderService.class);
        }
    }

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(orderServiceMock))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    void createOrder_shouldDocument() throws Exception {
        // Arrange
        var productDTO = new OrderProductDTO();
        productDTO.setProductId(1L);
        productDTO.setQuantity(2);

        var request = new CreateOrderDTO();
        request.setItems(List.of(productDTO));

        var response = OrderDTO.builder()
                .id(1L)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .products(List.of())
                .build();

        when(orderServiceMock.createOrder(any(CreateOrderDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()))
                .andDo(document("orders/create"));
    }


    @Test
    void payOrder_shouldDocument() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders/{id}/pay", 1L))
                .andExpect(status().isOk())
                .andDo(document("orders/pay"));
    }


    @Test
    void cancelOrder_shouldDocument() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders/{id}/cancel", 1L))
                .andExpect(status().isOk())
                .andDo(document("orders/cancel"));
    }

}
