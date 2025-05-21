package com.vserdiuk.casestudy.service.impl;

import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.dto.UpdateProductDTO;
import com.vserdiuk.casestudy.entity.Product;
import com.vserdiuk.casestudy.mapper.ProductMapper;
import com.vserdiuk.casestudy.repository.ProductRepository;
import com.vserdiuk.casestudy.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductDTO createProduct(CreateProductDTO dto) {
        log.info("Attempting to create product: {}", dto.getName());

        var name = dto.getName();
        if (productRepository.existsByName(name)) {
            throw new EntityNotFoundException("Product with name " + name + " already exists");
        }

        var product = Product.builder()
                .name(name)
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .build();

        var savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return ProductMapper.toDTO(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(UpdateProductDTO dto) {
        log.info("Attempting to update product with ID: {}", dto.getId());

        var id = dto.getId();
        var product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getStockQuantity() != null) product.setStockQuantity(dto.getStockQuantity());

        var updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());

        return ProductMapper.toDTO(updatedProduct);
    }

    public void deleteProduct(Long id) {
        log.info("Attempting to delete product with ID: {}", id);

        var product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);
        log.info("Product deleted successfully with ID: {}", id);
    }

    public List<ProductDTO> listProducts() {
        log.info("Listing all products");

        return productRepository.findAll().stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }
}
