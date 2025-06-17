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
import com.vserdiuk.casestudy.validator.OrderValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    public static final int EXPIRATION_DURATION = 30;
    private static final int MAX_RETRIES = 3;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderExpirationProducer orderExpirationProducer;
    private final OrderValidator orderValidator;

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderDTO dto) {
        Set<Long> productIds = extractProductIds(dto);
        Map<Long, Product> productMap = fetchProductsWithLock(productIds);
        validateStockAvailability(dto, productMap);

        Order order = createNewOrder();
        List<OrderProduct> orderProducts = reserveStockAndCreateOrderProducts(dto, productMap, order);

        saveOrderAndProducts(productMap.values(), order, orderProducts);
        scheduleOrderExpiration(order.getId());

        asyncLog("Order created successfully with ID: {}", order.getId());
        return OrderMapper.toDTO(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        executeWithRetry(() -> {
            Order order = orderValidator.validateOrderIsCreated(orderId, "canceled");
            releaseReservedStock(order);
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            asyncLog("Order with ID: {} canceled and stock released", orderId);
        });
    }

    @Override
    @Transactional
    public void payOrder(Long orderId) {
        executeWithRetry(() -> {
            Order order = orderValidator.validateOrderIsCreated(orderId, "paid");
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            orderRepository.save(order);
            asyncLog("Order with ID: {} marked as PAID", orderId);
        });
    }

    @Override
    @Transactional
    public void expireOrderById(Long orderId) {
        executeWithRetry(() -> {
            Order order = orderValidator.validateOrderForExpiration(orderId);
            if (order == null) {
                asyncLog("Order with ID: {} is already processed. Skipping expiration.", orderId);
                return;
            }
            releaseReservedStock(order);
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
            asyncLog("Order with ID: {} has been expired and stock released", orderId);
        });
    }

    @Cacheable(value = "products", key = "#productIds")
    public List<Product> findProductsByIds(Set<Long> productIds) {
        return productRepository.findAllById(productIds);
    }

    @Async
    public void asyncLog(String message, Object... args) {
        log.info(message, args);
    }

    /**
     * Executes a given operation with retry logic in case of OptimisticLockException.
     * The operation is retried up to MAX_RETRIES times with exponential backoff.
     *
     * @param operation The operation to execute, provided as a Runnable.
     * @throws BusinessException if the operation fails after all retries or if interrupted.
     */
    private void executeWithRetry(Runnable operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                operation.run();
                return;
            } catch (OptimisticLockException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new BusinessException("Failed to process operation after " + MAX_RETRIES + " attempts due to concurrent modifications");
                }
                asyncLog("Retrying operation due to OptimisticLockException, attempt: {}", attempt);
                try {
                    Thread.sleep(100 * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException("Interrupted during retry");
                }
            }
        }
    }

    /**
     * Retrieves an Order by its ID with an optimistic lock to prevent concurrent modifications.
     *
     * @param orderId The ID of the order to retrieve.
     * @return The Order entity.
     * @throws EntityNotFoundException if no order is found with the given ID.
     */
    @Lock(LockModeType.OPTIMISTIC)
    private Order getOrderWithLock(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));
    }

    /**
     * Releases the reserved stock for all products associated with the given order.
     * Iterates through the order's products and increments their stock quantities.
     *
     * @param order The Order entity whose products' stock needs to be released.
     */
    private void releaseReservedStock(Order order) {
        List<Product> productsToUpdate = new ArrayList<>();
        order.getOrderProducts().forEach(orderProduct -> {
            Product product = orderProduct.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderProduct.getQuantity());
            productsToUpdate.add(product);
        });
        productRepository.saveAll(productsToUpdate);
    }

    /**
     * Extracts product IDs from the CreateOrderDTO.
     *
     * @param dto The CreateOrderDTO containing the order items.
     * @return A Set of product IDs extracted from the DTO.
     */
    private Set<Long> extractProductIds(CreateOrderDTO dto) {
        return dto.getItems().stream()
                .map(OrderProductDTO::getProductId)
                .collect(Collectors.toSet());
    }

    /**
     * Fetches products by their IDs with a lock to ensure data consistency during order processing.
     *
     * @param productIds The set of product IDs to fetch.
     * @return A Map of product IDs to Product entities.
     */
    private Map<Long, Product> fetchProductsWithLock(Set<Long> productIds) {
        List<Product> products = productRepository.findAllByIdWithLock(productIds);
        return products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    /**
     * Validates stock availability for the products in the order.
     * Checks if products exist and have sufficient stock for the requested quantities.
     *
     * @param dto        The CreateOrderDTO containing the order items.
     * @param productMap A Map of product IDs to Product entities.
     * @throws BusinessException if stock validation fails.
     */
    private void validateStockAvailability(CreateOrderDTO dto, Map<Long, Product> productMap) {
        List<String> errors = getErrors(dto, productMap);

        if (!errors.isEmpty()) {
            asyncLog("Order creation failed due to stock issues: {}", errors);
            throw new BusinessException(String.join("; ", errors));
        }
    }

    /**
     * Generates a list of error messages for products that are either not found or have insufficient stock.
     *
     * @param dto        The CreateOrderDTO containing the order items.
     * @param productMap A Map of product IDs to Product entities.
     * @return A List of error messages.
     */
    private static List<String> getErrors(CreateOrderDTO dto, Map<Long, Product> productMap) {
        List<String> errors = new ArrayList<>();
        for (OrderProductDTO item : dto.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                errors.add("Product not found with id: " + item.getProductId());
            } else if (product.getStockQuantity() < item.getQuantity()) {
                errors.add("Insufficient stock for product: " + product.getName());
            }
        }
        return errors;
    }

    /**
     * Creates a new Order entity with the CREATED status and current timestamp.
     *
     * @return The newly created Order entity.
     */
    private Order createNewOrder() {
        return Order.builder()
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Reserves stock for the products in the order and creates corresponding OrderProduct entities.
     * Updates the stock quantities in the product map and associates products with the order.
     *
     * @param dto        The CreateOrderDTO containing the order items.
     * @param productMap A Map of product IDs to Product entities.
     * @param order      The Order entity to associate with the products.
     * @return A List of OrderProduct entities.
     */
    private List<OrderProduct> reserveStockAndCreateOrderProducts(CreateOrderDTO dto,
                                                                  Map<Long, Product> productMap,
                                                                  Order order) {
        return dto.getItems().stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                    return OrderProduct.builder()
                            .order(order)
                            .product(product)
                            .quantity(item.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Saves the order and its associated products to the database.
     * Persists updated product stock quantities and links order products to the order.
     *
     * @param products      The Collection of Product entities to save.
     * @param order         The Order entity to save.
     * @param orderProducts The List of OrderProduct entities to associate with the order.
     */
    private void saveOrderAndProducts(Collection<Product> products, Order order, List<OrderProduct> orderProducts) {
        productRepository.saveAll(products);
        order.setOrderProducts(orderProducts);
        orderRepository.save(order);
    }

    /**
     * Schedules an expiration event for the order using the OrderExpirationProducer.
     *
     * @param orderId The ID of the order to schedule for expiration.
     */
    private void scheduleOrderExpiration(Long orderId) {
        orderExpirationProducer.scheduleExpiration(orderId, EXPIRATION_DURATION);
    }
}