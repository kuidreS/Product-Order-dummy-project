package com.vserdiuk.casestudy.service.impl;

import com.vserdiuk.casestudy.dto.CreateOrderDTO;
import com.vserdiuk.casestudy.dto.OrderDTO;
import com.vserdiuk.casestudy.dto.OrderProductDTO;
import com.vserdiuk.casestudy.entity.Order;
import com.vserdiuk.casestudy.entity.OrderProduct;
import com.vserdiuk.casestudy.entity.OrderStatus;
import com.vserdiuk.casestudy.entity.Product;
import com.vserdiuk.casestudy.exception.BusinessException;
import com.vserdiuk.casestudy.mapper.OrderMapper;
import com.vserdiuk.casestudy.messaging.OrderExpirationProducer;
import com.vserdiuk.casestudy.repository.OrderRepository;
import com.vserdiuk.casestudy.repository.ProductRepository;
import com.vserdiuk.casestudy.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    public static final int EXPIRATION_DURATION = 30;

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final OrderExpirationProducer orderExpirationProducer;

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderDTO dto) {
        log.info("Creating order with items: {}", dto.getItems());

        // Load and validate all products first
        Map<Long, Product> productMap = productRepository.findAllById(
                dto.getItems().stream().map(OrderProductDTO::getProductId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<String> insufficientStockErrors = new ArrayList<>();

        for (OrderProductDTO item : dto.getItems()) {
            var product = productMap.get(item.getProductId());
            if (product == null) {
                insufficientStockErrors.add("Product not found with id: " + item.getProductId());
            } else if (product.getStockQuantity() < item.getQuantity()) {
                insufficientStockErrors.add("Insufficient stock for product: " + product.getName());
            }
        }

        if (!insufficientStockErrors.isEmpty()) {
            log.warn("Order creation failed due to stock issues: {}", insufficientStockErrors);
            throw new BusinessException(String.join("; ", insufficientStockErrors));
        }

        // Reserve stock and create order
        var order = Order.builder()
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderProduct> orderProducts = dto.getItems().stream().map(item -> {
            var product = productMap.get(item.getProductId());
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);

            return OrderProduct.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        order.setOrderProducts(orderProducts);
        var savedOrder = orderRepository.save(order);

        // Schedule expiration in 30 minutes
        orderExpirationProducer.scheduleExpiration(savedOrder.getId(), EXPIRATION_DURATION);

        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return OrderMapper.toDTO(savedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Only CREATED orders can be canceled");
        }

        releaseReservedStock(order);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order with ID: {} canceled and stock released", orderId);
    }

    @Override
    @Transactional
    public void payOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Only CREATED orders can be paid");
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order with ID: {} marked as PAID", orderId);
    }

    @Override
    @Transactional
    public void expireOrderById(Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            log.info("Order with ID: {} is already processed. Skipping expiration.", orderId);
            return;
        }

        releaseReservedStock(order);
        order.setStatus(OrderStatus.EXPIRED);
        orderRepository.save(order);

        log.info("Order with ID: {} has been expired and stock released", orderId);
    }

    private void releaseReservedStock(Order order) {
        order.getOrderProducts().forEach(orderProduct -> {
            Product product = orderProduct.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderProduct.getQuantity());
            productRepository.save(product);
        });
    }
}
