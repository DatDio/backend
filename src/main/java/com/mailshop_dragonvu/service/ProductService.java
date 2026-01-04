package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.products.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    List<ProductResponseClientDTO> getAllActiveProducts();
    // Admin operations
    ProductResponseDTO createProduct(ProductCreateDTO request);

    ProductResponseDTO updateProduct(Long id, ProductUpdateDTO request);

    ProductResponseDTO getProductById(Long id);

    void deleteProduct(Long id);

    void activateProduct(Long id);

    void deactivateProduct(Long id);
    Page<ProductResponseDTO> searchProducts(ProductFilterDTO request);
}
