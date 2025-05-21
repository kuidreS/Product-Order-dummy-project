package com.vserdiuk.casestudy.service;

import com.vserdiuk.casestudy.dto.CreateProductDTO;
import com.vserdiuk.casestudy.dto.ProductDTO;
import com.vserdiuk.casestudy.dto.UpdateProductDTO;

import java.util.List;

public interface ProductService {

    ProductDTO createProduct(CreateProductDTO dto);

    ProductDTO updateProduct(UpdateProductDTO dto);

    void deleteProduct(Long id);

    List<ProductDTO> listProducts();
}
