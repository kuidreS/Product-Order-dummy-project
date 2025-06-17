# ProductServiceImpl Test Scenarios

## 1. Create Product Successfully
**Purpose**: Verify that a product can be created successfully with valid input.

**Setup**:
- Create a `CreateProductDTO` with name "Test Product", price $10.0, and stock quantity 100.
- Mock `ProductRepository` to return `false` for `existsByName("Test Product")`.
- Mock `ProductRepository.save` to return a product with ID 1, matching the DTO values.
- Mock `ProductValidator` to validate the DTO without throwing exceptions.

**Action**:
- Call `productService.createProduct(dto)`.

**Expected Outcome**:
- Returns a non-null `ProductDTO` with ID 1, name "Test Product", price $10.0, and stock quantity 100.
- Verifies that `productValidator.validateDTO` and `productRepository.save` are called.

## 2. Fail to Create Product with Existing Name
**Purpose**: Ensure product creation fails when a product with the same name already exists.

**Setup**:
- Create a `CreateProductDTO` with name "Existing Product", price $10.0, and stock quantity 100.
- Mock `ProductRepository` to return `true` for `existsByName("Existing Product")`.
- Mock `ProductValidator` to validate the DTO without throwing exceptions.

**Action**:
- Call `productService.createProduct(dto)`.

**Expected Outcome**:
- Throws `IllegalArgumentException` with message containing "Product with name Existing Product already exists".
- Verifies that `productValidator.validateDTO` is called and `productRepository.save` is not called.

## 3. Create Multiple Products Successfully
**Purpose**: Verify that multiple products can be created successfully with valid inputs.

**Setup**:
- Create two `CreateProductDTO` objects: one with name "Product 1", price $10.0, stock 100; another with name "Product 2", price $20.0, stock 200.
- Mock `ProductRepository.findByNameIn` to return an empty list.
- Mock `ProductRepository.saveAll` to return two products with IDs 1 and 2, matching the DTO values.
- Mock `ProductValidator` to validate both DTOs without throwing exceptions.

**Action**:
- Call `productService.createProducts(dtos)`.

**Expected Outcome**:
- Returns a list of two `ProductDTO` objects with names "Product 1" and "Product 2".
- Verifies that `productValidator.validateDTO` is called twice and `productRepository.saveAll` is called once.

## 4. Fail to Create Multiple Products with Duplicate Names
**Purpose**: Ensure creation of multiple products fails when duplicate product names are provided.

**Setup**:
- Create two `CreateProductDTO` objects, both with name "Product".
- Mock `ProductValidator` to validate both DTOs without throwing exceptions.

**Action**:
- Call `productService.createProducts(dtos)`.

**Expected Outcome**:
- Throws `IllegalArgumentException` with message containing "Duplicate Product names found".
- Verifies that `productRepository.saveAll` is not called.

## 5. Update Product Successfully
**Purpose**: Verify that an existing product can be updated successfully.

**Setup**:
- Create an `UpdateProductDTO` with ID 1, name "Updated Name", price $20.0, and stock quantity 50.
- Mock `ProductRepository.findById(1L)` to return a product with ID 1, name "Old Name", price $10.0, and stock 100.
- Mock `ProductRepository.existsByName("Updated Name")` to return `false`.
- Mock `ProductRepository.save` to return the updated product.
- Mock `ProductValidator` to validate the DTO without throwing exceptions.

**Action**:
- Call `productService.updateProduct(dto)`.

**Expected Outcome**:
- Returns a `ProductDTO` with name "Updated Name", price $20.0, and stock quantity 50.
- Verifies that `productValidator.validateDTO` and `productRepository.save` are called.

## 6. Fail to Update Non-Existing Product
**Purpose**: Ensure product update fails when the product does not exist.

**Setup**:
- Create an `UpdateProductDTO` with ID 1.
- Mock `ProductRepository.findById(1L)` to return an empty Optional.
- Mock `ProductValidator` to validate the DTO without throwing exceptions.

**Action**:
- Call `productService.updateProduct(dto)`.

**Expected Outcome**:
- Throws `EntityNotFoundException` with message containing "Product not found with id: 1".
- Verifies that `productValidator.validateDTO` is called and `productRepository.save` is not called.

## 7. Update Multiple Products Successfully
**Purpose**: Verify that multiple existing products can be updated successfully.

**Setup**:
- Create two `UpdateProductDTO` objects: one with ID 1, name "Updated Product 1", price $15.0, stock 150; another with ID 2, name "Updated Product 2", price $25.0, stock 250.
- Mock `ProductRepository.findByNameIn` to return an empty list.
- Mock `ProductRepository.findById` for IDs 1 and 2 to return existing products.
- Mock `ProductRepository.saveAll` to return the updated products.
- Mock `ProductValidator` to validate both DTOs without throwing exceptions.

**Action**:
- Call `productService.updateProducts(dtos)`.

**Expected Outcome**:
- Returns a list of two `ProductDTO` objects with names "Updated Product 1" and "Updated Product 2".
- Verifies that `productValidator.validateDTO` is called twice and `productRepository.saveAll` is called once.

## 8. Fail to Update Multiple Products with Duplicate Names
**Purpose**: Ensure updating multiple products fails when duplicate product names are provided.

**Setup**:
- Create two `UpdateProductDTO` objects with IDs 1 and 2, both with name "Product".
- Mock `ProductValidator` to validate both DTOs without throwing exceptions.

**Action**:
- Call `productService.updateProducts(dtos)`.

**Expected Outcome**:
- Throws `IllegalArgumentException` with message containing "Duplicate Product names found".
- Verifies that `productRepository.saveAll` is not called.

## 9. Delete Product Successfully
**Purpose**: Verify that an existing product can be deleted successfully.

**Setup**:
- Mock `ProductRepository.existsById(1L)` to return `true`.

**Action**:
- Call `productService.deleteProduct(1L)`.

**Expected Outcome**:
- Verifies that `productRepository.deleteById(1L)` is called.

## 10. Fail to Delete Non-Existing Product
**Purpose**: Ensure deletion fails when the product does not exist.

**Setup**:
- Mock `ProductRepository.existsById(1L)` to return `false`.

**Action**:
- Call `productService.deleteProduct(1L)`.

**Expected Outcome**:
- Throws `EntityNotFoundException` with message containing "Product not found with id: 1".
- Verifies that `productRepository.deleteById` is not called.

## 11. Delete Multiple Products Successfully
**Purpose**: Verify that multiple existing products can be deleted successfully.

**Setup**:
- Create a list of IDs [1L, 2L].
- Mock `ProductRepository.findAllById` to return two products with IDs 1 and 2.

**Action**:
- Call `productService.deleteProducts(ids)`.

**Expected Outcome**:
- Verifies that `productRepository.deleteAllById` is called with the provided IDs.

## 12. Fail to Delete with Duplicate IDs
**Purpose**: Ensure deletion of multiple products fails when duplicate IDs are provided.

**Setup**:
- Create a list of IDs [1L, 1L].

**Action**:
- Call `productService.deleteProducts(ids)`.

**Expected Outcome**:
- Throws `IllegalArgumentException` with message containing "Duplicate Product IDs found".
- Verifies that `productRepository.deleteAllById` is not called.

## 13. Fail to Delete Non-Existing Products
**Purpose**: Ensure deletion fails when some products do not exist.

**Setup**:
- Create a list of IDs [1L, 2L].
- Mock `ProductRepository.findAllById` to return only one product with ID 1.

**Action**:
- Call `productService.deleteProducts(ids)`.

**Expected Outcome**:
- Throws `EntityNotFoundException` with message containing "Products not found with ids: [2]".
- Verifies that `productRepository.deleteAllById` is not called.

## 14. Get Product Successfully
**Purpose**: Verify that an existing product can be retrieved successfully.

**Setup**:
- Mock `ProductRepository.findById(1L)` to return a product with ID 1, name "Test Product", price $10.0, and stock 100.

**Action**:
- Call `productService.getProduct(1L)`.

**Expected Outcome**:
- Returns a `ProductDTO` with ID 1, name "Test Product", price $10.0, and stock 100.
- Verifies that `productRepository.findById(1L)` is called.

## 15. Fail to Get Non-Existing Product
**Purpose**: Ensure retrieval fails when the product does not exist.

**Setup**:
- Mock `ProductRepository.findById(1L)` to return an empty Optional.

**Action**:
- Call `productService.getProduct(1L)`.

**Expected Outcome**:
- Throws `EntityNotFoundException` with message containing "Product not found with id: 1".

## 16. Get All Products Successfully
**Purpose**: Verify that all products can be retrieved successfully with pagination.

**Setup**:
- Mock `ProductRepository.findAll` with `PageRequest.of(0, 10)` to return a page containing two products: ID 1, name "Product 1", price $10.0, stock 100; ID 2, name "Product 2", price $20.0, stock 200.

**Action**:
- Call `productService.getAllProducts(pageable)`.

**Expected Outcome**:
- Returns a `Page<ProductDTO>` with two products, names "Product 1" and "Product 2".
- Verifies that `productRepository.findAll` is called with the provided pageable.