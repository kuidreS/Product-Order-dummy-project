package com.vserdiuk.casestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vserdiuk.casestudy.dto.CreateOrderDTO;
import com.vserdiuk.casestudy.dto.OrderDTO;
import com.vserdiuk.casestudy.dto.OrderProductDTO;
import com.vserdiuk.casestudy.entity.OrderStatus;
import com.vserdiuk.casestudy.exception.BusinessException;
import com.vserdiuk.casestudy.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link OrderController}.
 * <p>
 * This test class verifies the behavior of the OrderController endpoints using MockMvc and Mockito.
 * It tests the creation, payment, and cancellation of orders, ensuring proper HTTP responses and
 * REST documentation generation.
 * </p>
 */
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(OrderController.class)
@AutoConfigureRestDocs
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    /**
     * Tests the creation of a new order via the POST /api/orders endpoint.
     * <p>
     * Verifies that the endpoint returns a 201 Created status, the correct order details in the response,
     * and generates REST documentation for the operation.
     * </p>
     *
     * @throws Exception if the test execution fails
     */
    @Test
    void createOrder_shouldReturnCreatedOrder_andDocument() throws Exception {
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
                .paidAt(null)
                .products(List.of())
                .build();

        when(orderService.createOrder(any(CreateOrderDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.paidAt").doesNotExist())
                .andExpect(jsonPath("$.products").isArray())
                .andDo(document("orders/create",
                        requestFields(
                                fieldWithPath("items").description("List of order items"),
                                fieldWithPath("items[].productId").description("ID of the product"),
                                fieldWithPath("items[].quantity").description("Quantity of the product")
                        ),
                        responseFields(
                                fieldWithPath("id").description("ID of the created order"),
                                fieldWithPath("status").description("Status of the order"),
                                fieldWithPath("createdAt").description("Timestamp when the order was created"),
                                fieldWithPath("paidAt").description("Timestamp when the order was paid, null if not paid").optional(),
                                fieldWithPath("products").description("List of products in the order")
                        )
                ));
    }

    /**
     * Tests the payment of an existing order via the POST /api/orders/{id}/pay endpoint.
     * <p>
     * Verifies that the endpoint returns a 200 OK status, the order service's payOrder method is called,
     * and generates REST documentation for the operation.
     * </p>
     *
     * @throws Exception if the test execution fails
     */
    @Test
    void payOrder_shouldReturnOk_andDocument() throws Exception {
        // Arrange
        Long orderId = 1L;
        doNothing().when(orderService).payOrder(eq(orderId));

        // Act & Assert
        mockMvc.perform(post("/api/orders/{id}/pay", orderId))
                .andExpect(status().isOk())
                .andDo(document("orders/pay",
                        pathParameters(
                                parameterWithName("id").description("ID of the order to pay")
                        )
                ));
    }

    /**
     * Tests the cancellation of an existing order via the POST /api/orders/{id}/cancel endpoint.
     * <p>
     * Verifies that the endpoint returns a 200 OK status, the order service's cancelOrder method is called,
     * and generates REST documentation for the operation.
     * </p>
     *
     * @throws Exception if the test execution fails
     */
    @Test
    void cancelOrder_shouldReturnOk_andDocument() throws Exception {
        // Arrange
        Long orderId = 1L;
        doNothing().when(orderService).cancelOrder(eq(orderId));

        // Act & Assert
        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andDo(document("orders/cancel",
                        pathParameters(
                                parameterWithName("id").description("ID of the order to cancel")
                        )
                ));
    }

    /**
     * Tests the creation of an order with insufficient stock, expecting a 400 Bad Request.
     * <p>
     * Verifies that the endpoint returns a 400 status when a BusinessException is thrown
     * due to insufficient stock, and generates REST documentation for the error case.
     * </p>
     *
     * @throws Exception if the test execution fails
     */
    @Test
    void createOrder_withInsufficientStock_shouldReturnBadRequest() throws Exception {
        // Arrange
        var productDTO = new OrderProductDTO();
        productDTO.setProductId(1L);
        productDTO.setQuantity(100);

        var request = new CreateOrderDTO();
        request.setItems(List.of(productDTO));

        when(orderService.createOrder(any(CreateOrderDTO.class)))
                .thenThrow(new BusinessException("Insufficient stock for product: Test Product"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("orders/create-error",
                        requestFields(
                                fieldWithPath("items").description("List of order items"),
                                fieldWithPath("items[].productId").description("ID of the product"),
                                fieldWithPath("items[].quantity").description("Quantity of the product")
                        )
                ));
    }
}