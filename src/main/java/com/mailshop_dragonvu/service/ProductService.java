package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.products.ProductCreateDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.dto.products.ProductFilterDTO;
import com.mailshop_dragonvu.dto.products.ProductUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    // Admin operations
    ProductResponseDTO createProduct(ProductCreateDTO request);

    ProductResponseDTO updateProduct(Long id, ProductUpdateDTO request);

    ProductResponseDTO getProductById(Long id);

    void deleteProduct(Long id);

    void activateProduct(Long id);

    void deactivateProduct(Long id);
    Page<ProductResponseDTO> searchProducts(ProductFilterDTO request);
}
