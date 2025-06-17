# OrderServiceImpl Test Scenarios

## 1. Create Order Successfully
**Purpose**: Verify that an order can be created successfully when products have sufficient stock.

**Setup**:
- Mock `ProductRepository` to return a product with ID 1, name "Product 1", price $10.0, and stock quantity 10.
- Mock `OrderRepository` to save an order and assign it ID 1.
- Mock `ProductRepository.saveAll` to return the updated product list.
- Create a `CreateOrderDTO` with one `OrderProductDTO` (product ID 1, quantity 2).

**Action**:
- Call `orderService.createOrder(createOrderDTO)`.

**Expected Outcome**:
- Returns a non-null `OrderDTO`.
- Verifies that `productRepository.saveAll` and `orderRepository.save` are called.
- Verifies that `orderExpirationProducer.scheduleExpiration` is called with order ID 1 and 30-minute expiration.
- Product stock quantity is reduced to 8.

## 2. Fail to Create Order Due to Insufficient Stock
**Purpose**: Ensure order creation fails when a product has insufficient stock.

**Setup**:
- Mock `ProductRepository` to return a product with ID 1, name "Product 1", price $10.0, and stock quantity 1.
- Create a `CreateOrderDTO` with one `OrderProductDTO` (product ID 1, quantity 5).

**Action**:
- Call `orderService.createOrder(createOrderDTO)`.

**Expected Outcome**:
- Throws `BusinessException` with message containing "Insufficient stock for product: Product 1".
- Verifies that `productRepository.saveAll` and `orderRepository.save` are not called.

## 3. Fail to Create Order Due to Non-Existing Product
**Purpose**: Ensure order creation fails when a product does not exist.

**Setup**:
- Mock `ProductRepository` to return an empty list for product ID 99.
- Create a `CreateOrderDTO` with one `OrderProductDTO` (product ID 99, quantity 1).

**Action**:
- Call `orderService.createOrder(createOrderDTO)`.

**Expected Outcome**:
- Throws `BusinessException` with message containing "Product not found with id: 99".
- Verifies that `productRepository.saveAll` and `orderRepository.save` are not called.

## 4. Cancel Order Successfully
**Purpose**: Verify that a created order can be canceled, restoring product stock.

**Setup**:
- Mock `OrderValidator` to return an order with ID 1, status CREATED, containing one `OrderProduct` (product ID 1, quantity 2).
- Mock `ProductRepository` to return a product with ID 1, stock quantity 8.
- Mock `ProductRepository.saveAll` and `OrderRepository.save` to return updated entities.

**Action**:
- Call `orderService.cancelOrder(1L)`.

**Expected Outcome**:
- Verifies that `productRepository.saveAll` and `orderRepository.save` are called.
- Order status is updated to CANCELLED.
- Product stock quantity is restored to 10.

## 5. Fail to Cancel Non-Created Order
**Purpose**: Ensure that only orders in CREATED status can be canceled.

**Setup**:
- Mock `OrderValidator` to throw `BusinessException` with message "Only CREATED orders can be canceled" for order ID 1.

**Action**:
- Call `orderService.cancelOrder(1L)`.

**Expected Outcome**:
- Throws `BusinessException` with message "Only CREATED orders can be canceled".
- Verifies that `productRepository.saveAll` and `orderRepository.save` are not called.

## 6. Pay Order Successfully
**Purpose**: Verify that a created order can be paid, updating its status and paid timestamp.

**Setup**:
- Mock `OrderValidator` to return an order with ID 1, status CREATED.
- Mock `OrderRepository.save` to return the updated order.

**Action**:
- Call `orderService.payOrder(1L)`.

**Expected Outcome**:
- Verifies that `orderRepository.save` is called.
- Order status is updated to PAID.
- Order `paidAt` timestamp is set.

## 7. Fail to Pay Non-Created Order
**Purpose**: Ensure that only orders in CREATED status can be paid.

**Setup**:
- Mock `OrderValidator` to throw `BusinessException` with message "Only CREATED orders can be paid" for order ID 1.

**Action**:
- Call `orderService.payOrder(1L)`.

**Expected Outcome**:
- Throws `BusinessException` with message "Only CREATED orders can be paid".
- Verifies that `orderRepository.save` is not called.

## 8. Expire Order Successfully
**Purpose**: Verify that a created order can be expired, restoring product stock.

**Setup**:
- Mock `OrderValidator` to return an order with ID 1, status CREATED, containing one `OrderProduct` (product ID 1, quantity 2).
- Mock `ProductRepository` to return a product with ID 1, stock quantity 8.
- Mock `ProductRepository.saveAll` and `OrderRepository.save` to return updated entities.

**Action**:
- Call `orderService.expireOrderById(1L)`.

**Expected Outcome**:
- Verifies that `productRepository.saveAll` and `orderRepository.save` are called.
- Order status is updated to EXPIRED.
- Product stock quantity is restored to 10.

## 9. Skip Expiration for Non-Created Order
**Purpose**: Ensure that non-created orders are skipped during expiration.

**Setup**:
- Mock `OrderValidator` to return null for order ID 1.

**Action**:
- Call `orderService.expireOrderById(1L)`.

**Expected Outcome**:
- Verifies that `productRepository.saveAll` and `orderRepository.save` are not called.
- No exception is thrown.