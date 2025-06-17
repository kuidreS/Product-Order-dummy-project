package com.vserdiuk.casestudy.controller;

import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.dto.UpdateProductDTO;
import com.vserdiuk.casestudy.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Product-related operations.
 * <p>
 * This controller provides endpoints for creating, updating, deleting, and retrieving Products.
 * It supports both single and batch operations for product management, as well as paginated retrieval of products.
 * All endpoints are prefixed with "/api/products".
 * </p>
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product API", description = "Operations related to Products management")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Creates a new product based on the provided data.
     *
     * @param dto the data transfer object containing the details required to create a product
     * @return a {@link ResponseEntity} containing the created {@link ProductDTO} with HTTP status 201 (Created)
     */
    @Operation(summary = "Create a new product")
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductDTO dto) {
        return new ResponseEntity<>(productService.createProduct(dto), HttpStatus.CREATED);
    }

    /**
     * Creates multiple products in a single batch operation.
     *
     * @param dtos a list of data transfer objects containing the details for creating multiple products
     * @return a {@link ResponseEntity} containing a list of created {@link ProductDTO} objects with HTTP status 201 (Created)
     */
    @Operation(summary = "Create multiple products")
    @PostMapping("/batch")
    public ResponseEntity<List<ProductDTO>> createProducts(@Valid @RequestBody List<CreateProductDTO> dtos) {
        return new ResponseEntity<>(productService.createProducts(dtos), HttpStatus.CREATED);
    }

    /**
     * Updates an existing product with the provided data.
     *
     * @param dto the data transfer object containing the updated product information
     * @return a {@link ResponseEntity} containing the updated {@link ProductDTO} with HTTP status 200 (OK)
     */
    @Operation(summary = "Update an existing product")
    @PutMapping
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody UpdateProductDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(dto));
    }

    /**
     * Updates multiple existing products in a single batch operation.
     *
     * @param dtos a list of data transfer objects containing the updated information for multiple products
     * @return a {@link ResponseEntity} containing a list of updated {@link ProductDTO} objects with HTTP status 200 (OK)
     */
    @Operation(summary = "Update multiple products")
    @PutMapping("/batch")
    public ResponseEntity<List<ProductDTO>> updateProducts(@Valid @RequestBody List<UpdateProductDTO> dtos) {
        return ResponseEntity.ok(productService.updateProducts(dtos));
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the ID of the product to delete
     * @return a {@link ResponseEntity} with HTTP status 204 (No Content) indicating successful deletion
     */
    @Operation(summary = "Delete a Product by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes multiple products by their IDs in a single batch operation.
     *
     * @param ids a list of product IDs to delete
     * @return a {@link ResponseEntity} with HTTP status 204 (No Content) indicating successful deletion
     */
    @Operation(summary = "Delete multiple Products by their IDs")
    @DeleteMapping
    public ResponseEntity<Void> deleteProducts(@RequestBody List<Long> ids) {
        productService.deleteProducts(ids);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the ID of the product to retrieve
     * @return a {@link ResponseEntity} containing the {@link ProductDTO} with HTTP status 200 (OK)
     */
    @Operation(summary = "Get a Product by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    /**
     * Retrieves a paginated list of all products.
     *
     * @param pageable pagination and sorting information
     * @return a {@link Page} of {@link ProductDTO} objects containing the paginated list of products
     */
    @Operation(summary = "List all Products with pagination")
    @GetMapping
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productService.getAllProducts(pageable);
    }
}