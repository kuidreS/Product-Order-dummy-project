/**
 * Unit tests for the {@link OrderServiceImpl} class, which handles order-related operations
 * such as creation, cancellation, payment, and expiration of orders.
 */
package com.vserdiuk.casestudy.service.impl;

import com.vserdiuk.casestudy.dto.CreateOrderDTO;
import com.vserdiuk.casestudy.dto.OrderDTO;
import com.vserdiuk.casestudy.dto.OrderProductDTO;
import com.vserdiuk.casestudy.entity.Order;
import com.vserdiuk.casestudy.entity.OrderProduct;
import com.vserdiuk.casestudy.entity.OrderStatus;
import com.vserdiuk.casestudy.entity.Product;
import com.vserdiuk.casestudy.exception.BusinessException;
import com.vserdiuk.casestudy.messaging.OrderExpirationProducer;
import com.vserdiuk.casestudy.repository.OrderRepository;
import com.vserdiuk.casestudy.repository.ProductRepository;
import com.vserdiuk.casestudy.validator.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link OrderServiceImpl} using Mockito for mocking dependencies and AssertJ for assertions.
 */
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderExpirationProducer orderExpirationProducer;

    @Mock
    private OrderValidator orderValidator;

    @InjectMocks
    private OrderServiceImpl orderService;

    /**
     * Initializes mocks before each test method.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests successful order creation, verifying stock reduction and order persistence.
     */
    @Test
    void shouldCreateOrderSuccessfully() {
        // Arrange
        Product product = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(10)
                .build();
        OrderProductDTO orderProductDTO = new OrderProductDTO();
        orderProductDTO.setProductId(1L);
        orderProductDTO.setQuantity(2);
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setItems(List.of(orderProductDTO));

        Order order = Order.builder()
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        when(productRepository.findAllByIdWithLock(Set.of(1L))).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });
        when(productRepository.saveAll(any())).thenReturn(List.of(product));

        // Act
        OrderDTO result = orderService.createOrder(createOrderDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(productRepository).saveAll(any());
        verify(orderRepository).save(any(Order.class));
        verify(orderExpirationProducer).scheduleExpiration(1L, 30);
        assertThat(product.getStockQuantity()).isEqualTo(8); // Stock reduced by 2
    }

    /**
     * Tests order creation failure due to insufficient product stock.
     */
    @Test
    void shouldFailToCreateOrderDueToInsufficientStock() {
        // Arrange
        Product product = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(1)
                .build();
        OrderProductDTO orderProductDTO = new OrderProductDTO();
        orderProductDTO.setProductId(1L);
        orderProductDTO.setQuantity(5);
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setItems(List.of(orderProductDTO));

        when(productRepository.findAllByIdWithLock(Set.of(1L))).thenReturn(List.of(product));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock for product: Product 1");

        verify(productRepository, never()).saveAll(any());
        verify(orderRepository, never()).save(any());
    }

    /**
     * Tests order creation failure when a product does not exist.
     */
    @Test
    void shouldFailToCreateOrderDueToNonExistingProduct() {
        // Arrange
        OrderProductDTO orderProductDTO = new OrderProductDTO();
        orderProductDTO.setProductId(99L);
        orderProductDTO.setQuantity(1);
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setItems(List.of(orderProductDTO));

        when(productRepository.findAllByIdWithLock(Set.of(99L))).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(createOrderDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(productRepository, never()).saveAll(any());
        verify(orderRepository, never()).save(any());
    }

    /**
     * Tests successful order cancellation, verifying stock restoration and status update.
     */
    @Test
    void shouldCancelOrderSuccessfully() {
        // Arrange
        Product product = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(8)
                .build();
        OrderProduct orderProduct = OrderProduct.builder()
                .product(product)
                .quantity(2)
                .build();
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.CREATED)
                .orderProducts(List.of(orderProduct))
                .build();

        when(orderValidator.validateOrderIsCreated(1L, "canceled")).thenReturn(order);
        when(productRepository.saveAll(any())).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        orderService.cancelOrder(1L);

        // Assert
        verify(productRepository).saveAll(any());
        verify(orderRepository).save(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStockQuantity()).isEqualTo(10); // Stock restored
    }

    /**
     * Tests failure to cancel an order that is not in CREATED status.
     */
    @Test
    void shouldFailToCancelNonCreatedOrder() {
        // Arrange
        when(orderValidator.validateOrderIsCreated(1L, "canceled"))
                .thenThrow(new BusinessException("Only CREATED orders can be canceled"));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only CREATED orders can be canceled");

        verify(productRepository, never()).saveAll(any());
        verify(orderRepository, never()).save(any());
    }

    /**
     * Tests successful order payment, verifying status update and payment timestamp.
     */
    @Test
    void shouldPayOrderSuccessfully() {
        // Arrange
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.CREATED)
                .build();

        when(orderValidator.validateOrderIsCreated(1L, "paid")).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        orderService.payOrder(1L);

        // Assert
        verify(orderRepository).save(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
    }

    /**
     * Tests failure to pay an order that is not in CREATED status.
     */
    @Test
    void shouldFailToPayNonCreatedOrder() {
        // Arrange
        when(orderValidator.validateOrderIsCreated(1L, "paid"))
                .thenThrow(new BusinessException("Only CREATED orders can be paid"));

        // Act & Assert
        assertThatThrownBy(() -> orderService.payOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only CREATED orders can be paid");

        verify(orderRepository, never()).save(any());
    }

    /**
     * Tests successful order expiration, verifying stock restoration and status update.
     */
    @Test
    void shouldExpireOrderByIdSuccessfully() {
        // Arrange
        Product product = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(8)
                .build();
        OrderProduct orderProduct = OrderProduct.builder()
                .product(product)
                .quantity(2)
                .build();
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.CREATED)
                .orderProducts(List.of(orderProduct))
                .build();

        when(orderValidator.validateOrderForExpiration(1L)).thenReturn(order);
        when(productRepository.saveAll(any())).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        orderService.expireOrderById(1L);

        // Assert
        verify(productRepository).saveAll(any());
        verify(orderRepository).save(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        assertThat(product.getStockQuantity()).isEqualTo(10); // Stock restored
    }

    /**
     * Tests skipping expiration for an order that is not in CREATED status.
     */
    @Test
    void shouldSkipExpirationIfOrderIsNotCreated() {
        // Arrange
        when(orderValidator.validateOrderForExpiration(1L)).thenReturn(null);

        // Act
        orderService.expireOrderById(1L);

        // Assert
        verify(productRepository, never()).saveAll(any());
        verify(orderRepository, never()).save(any());
    }
}