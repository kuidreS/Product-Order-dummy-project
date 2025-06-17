package com.vserdiuk.casestudy.service;

import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.dto.UpdateProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing Products in the system.
 * <p>
 * Provides operations for creating, updating, retrieving, and deleting Products,
 * both individually and in batches.
 * Supports pagination for listing all Products.
 * </p>
 */
public interface ProductService {

    /**
     * Creates a new Product.
     *
     * @param dto the data transfer object containing the details required to create a Product
     * @return the created Product as a {@link ProductDTO}
     */
    ProductDTO createProduct(CreateProductDTO dto);

    /**
     * Creates multiple new Products.
     * <p>
     * Throws {@link jakarta.persistence.EntityNotFoundException} if any Product with the same name already exists.
     *
     * @param dtos a list of Product data to create
     * @return a list of created Products as {@link ProductDTO}
     */
    List<ProductDTO> createProducts(List<CreateProductDTO> dtos);

    /**
     * Updates an existing Product.
     * <p>
     * Throws {@link jakarta.persistence.EntityNotFoundException} if the Product is not found.
     *
     * @param dto the updated Product data
     * @return the updated Product as a {@link ProductDTO}
     */
    ProductDTO updateProduct(UpdateProductDTO dto);

    /**
     * Updates multiple existing products.
     * <p>
     * Throws {@link jakarta.persistence.EntityNotFoundException} if any Product ID is not found.
     *
     * @param dtos a list of updated Product data
     * @return a list of updated Products as {@link ProductDTO}
     */
    List<ProductDTO> updateProducts(List<UpdateProductDTO> dtos);

    /**
     * Deletes a Product by its ID.
     * <p>
     * Throws {@link jakarta.persistence.EntityNotFoundException} if the Product is not found.
     *
     * @param id the ID of the Product to delete
     */
    void deleteProduct(Long id);

    /**
     * Deletes multiple Products by their IDs.
     * <p>
     * Throws {@link jakarta.persistence.EntityNotFoundException} if any Product is not found.
     *
     * @param ids a list of Product IDs to delete
     */
    void deleteProducts(List<Long> ids);

    /**
     * Retrieves a paginated list of all products.
     *
     * @param pageable pagination and sorting information
     * @return a paginated list of Products as {@link ProductDTO}
     */
    Page<ProductDTO> getAllProducts(Pageable pageable);

    /**
     * Retrieves a Product by its ID.
     * <p>
     * Throws {@link jakarta.persistence.EntityNotFoundException} if the Product is not found.
     *
     * @param id the ID of the Product to retrieve
     * @return the Product as a {@link ProductDTO}
     */
    ProductDTO getProduct(Long id);
}
