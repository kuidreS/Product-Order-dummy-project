package com.vserdiuk.casestudy.service.impl;

import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.dto.UpdateProductDTO;
import com.vserdiuk.casestudy.entity.Product;
import com.vserdiuk.casestudy.mapper.ProductMapper;
import com.vserdiuk.casestudy.repository.ProductRepository;
import com.vserdiuk.casestudy.service.ProductService;
import com.vserdiuk.casestudy.validator.ProductValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    @Override
    @Transactional
    public ProductDTO createProduct(CreateProductDTO dto) {
        log.info("Creating product: {}", dto.getName());
        productValidator.validateDTO(dto);
        checkProductNameUniqueness(dto.getName());

        Product product = ProductMapper.toEntity(dto);
        Product savedProduct = productRepository.save(product);
        log.debug("Product created with ID: {}", savedProduct.getId());
        return ProductMapper.toDTO(savedProduct);
    }

    @Override
    @Transactional
    public List<ProductDTO> createProducts(List<CreateProductDTO> dtos) {
        log.info("Creating {} products", dtos.size());
        validateAndCheckDuplicates(dtos.stream().map(CreateProductDTO::getName).toList());

        List<Product> products = dtos.stream()
                .peek(productValidator::validateDTO)
                .map(ProductMapper::toEntity)
                .toList();

        return productRepository.saveAll(products).stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(UpdateProductDTO dto) {
        log.info("Updating Product ID: {}", dto.getId());
        productValidator.validateDTO(dto);

        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + dto.getId()));

        updateProductFields(product, dto);
        Product updatedProduct = productRepository.save(product);
        log.debug("Product updated with ID: {}", updatedProduct.getId());
        return ProductMapper.toDTO(updatedProduct);
    }

    @Override
    @Transactional
    public List<ProductDTO> updateProducts(List<UpdateProductDTO> dtos) {
        log.info("Updating {} products", dtos.size());
        validateAndCheckDuplicates(dtos.stream()
                .map(UpdateProductDTO::getName)
                .filter(name -> name != null)
                .toList());

        List<Product> products = dtos.stream()
                .peek(productValidator::validateDTO)
                .map(dto -> {
                    Product product = productRepository.findById(dto.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + dto.getId()));
                    updateProductFields(product, dto);
                    return product;
                })
                .toList();

        return productRepository.saveAll(products).stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting Product ID: {}", id);
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.debug("Product deleted with ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteProducts(List<Long> ids) {
        log.info("Deleting {} products", ids.size());
        Set<Long> uniqueIds = Set.copyOf(ids);
        if (uniqueIds.size() != ids.size()) {
            throw new IllegalArgumentException("Duplicate Product IDs found");
        }

        List<Long> existingIds = productRepository.findAllById(ids).stream()
                .map(Product::getId)
                .toList();

        if (existingIds.size() != ids.size()) {
            List<Long> missingIds = ids.stream()
                    .filter(id -> !existingIds.contains(id))
                    .toList();
            throw new EntityNotFoundException("Products not found with ids: " + missingIds);
        }

        productRepository.deleteAllById(ids);
        log.debug("Deleted {} products", ids.size());
    }

    @Override
    public ProductDTO getProduct(Long id) {
        log.info("Retrieving Product ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        return ProductMapper.toDTO(product);
    }

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.info("Retrieving products with pageable: {}", pageable);
        return productRepository.findAll(pageable).map(ProductMapper::toDTO);
    }

    /**
     * Checks if a Product with the specified name already exists in the repository
     *
     * @param name the name of the Product to check for uniqueness
     * @throws IllegalArgumentException if a Product with the given name already exists
     */
    private void checkProductNameUniqueness(String name) {
        if (productRepository.existsByName(name)) {
            throw new IllegalArgumentException("Product with name " + name + " already exists");
        }
    }

    /**
     * Validates that a list of Product names contains no duplicates and that no products
     * with these names already exist in the repository
     *
     * @param names a list of Product names to validate
     * @throws IllegalArgumentException if the input list contains duplicate names or if any
     *                                  products with the given names already exist in the repository
     */
    private void validateAndCheckDuplicates(List<String> names) {
        Set<String> uniqueNames = Set.copyOf(names);
        if (uniqueNames.size() != names.size()) {
            throw new IllegalArgumentException("Duplicate Product names found");
        }
        List<Product> existingProducts = productRepository.findByNameIn(names);
        if (!existingProducts.isEmpty()) {
            String existingNames = existingProducts.stream()
                    .map(Product::getName)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Products already exist: " + existingNames);
        }
    }

    /**
     * Updates the fields of a Product entity based on the provided DTO.
     * Only non-null fields in the DTO are applied to the Product
     *
     * @param product the Product entity to update
     * @param dto     the data transfer object containing the updated Product information
     * @throws IllegalArgumentException if the new name already exists for another Product
     */
    private void updateProductFields(Product product, UpdateProductDTO dto) {
        if (dto.getName() != null && !dto.getName().equals(product.getName())) {
            checkProductNameUniqueness(dto.getName());
            product.setName(dto.getName());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getStockQuantity() != null) {
            product.setStockQuantity(dto.getStockQuantity());
        }
    }
}