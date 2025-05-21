package com.vserdiuk.casestudy.service.impl;

import com.vserdiuk.casestudy.dto.CreateOrderDTO;
import com.vserdiuk.casestudy.dto.OrderProductDTO;
import com.vserdiuk.casestudy.entity.Order;
import com.vserdiuk.casestudy.entity.OrderProduct;
import com.vserdiuk.casestudy.entity.OrderStatus;
import com.vserdiuk.casestudy.entity.Product;
import com.vserdiuk.casestudy.exception.BusinessException;
import com.vserdiuk.casestudy.messaging.OrderExpirationProducer;
import com.vserdiuk.casestudy.repository.OrderRepository;
import com.vserdiuk.casestudy.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderExpirationProducer orderExpirationProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Arrange
        var product = Product.builder().id(1L).name("Product 1").price(BigDecimal.valueOf(10.0)).stockQuantity(10).build();
        var orderProductDTO = new OrderProductDTO();
        orderProductDTO.setProductId(1L);
        orderProductDTO.setQuantity(2);

        var createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setItems(List.of(orderProductDTO));

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(productRepository.save(any())).thenReturn(product);
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        orderService.createOrder(createOrderDTO);

        // Assert
        verify(productRepository).save(product);
        verify(orderRepository).save(any(Order.class));
        verify(orderExpirationProducer).scheduleExpiration(1L, 30);
    }

    @Test
    void shouldFailToCreateOrderDueToInsufficientStock() {
        // Arrange
        var product = Product.builder().id(1L).name("Product 1").price(BigDecimal.valueOf(10.0)).stockQuantity(1).build();
        var orderProductDTO = new OrderProductDTO();
        orderProductDTO.setProductId(1L);
        orderProductDTO.setQuantity(5);

        var createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setItems(List.of(orderProductDTO));

        when(productRepository.findAllById(any())).thenReturn(List.of(product));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");

        verify(productRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldFailToCreateOrderDueToNonExistingProduct() {
        // Arrange
        var orderProductDTO = new OrderProductDTO();
        orderProductDTO.setProductId(99L);
        orderProductDTO.setQuantity(1);

        var createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setItems(List.of(orderProductDTO));

        when(productRepository.findAllById(any())).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Product not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldCancelOrderSuccessfully() {
        // Arrange
        var product = Product.builder().id(1L).name("Product 1").price(BigDecimal.valueOf(10.0)).stockQuantity(10).build();
        var orderProduct = OrderProduct.builder().product(product).quantity(2).build();
        var order = Order.builder().id(1L).status(OrderStatus.CREATED).orderProducts(List.of(orderProduct)).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.cancelOrder(1L);

        // Assert
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldFailToCancelNonCreatedOrder() {
        // Arrange
        var order = Order.builder().id(1L).status(OrderStatus.PAID).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only CREATED orders can be canceled");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldPayOrderSuccessfully() {
        // Arrange
        var order = Order.builder().id(1L).status(OrderStatus.CREATED).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.payOrder(1L);

        // Assert
        verify(orderRepository).save(order);
    }

    @Test
    void shouldFailToPayNonCreatedOrder() {
        // Arrange
        var order = Order.builder().id(1L).status(OrderStatus.PAID).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.payOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only CREATED orders can be paid");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldExpireOrderByIdSuccessfully() {
        // Arrange
        var product = Product.builder().id(1L).name("Product 1").price(BigDecimal.valueOf(10.0)).stockQuantity(10).build();
        var orderProduct = OrderProduct.builder().product(product).quantity(2).build();
        var order = Order.builder().id(1L).status(OrderStatus.CREATED).orderProducts(List.of(orderProduct)).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.expireOrderById(1L);

        // Assert
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldSkipExpirationIfOrderIsNotCreated() {
        // Arrange
        var order = Order.builder().id(1L).status(OrderStatus.PAID).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.expireOrderById(1L);

        // Assert
        verify(orderRepository, never()).save(order);
    }

}