package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.products.ProductFilterDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.service.ProductService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(Constants.API_PATH.PRODUCTS)
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public ApiResponse<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO response = productService.getProductById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<Page<ProductResponseDTO>> searchProducts(ProductFilterDTO productFilterDTO) {
        Page<ProductResponseDTO> response = productService.searchProducts(productFilterDTO);
        return ApiResponse.success(response);
    }

    @GetMapping("/get-all")
    public ApiResponse<List<ProductResponseDTO>> getAllActiveProducts() {
        List<ProductResponseDTO> response = productService.getAllActiveProducts();
        return ApiResponse.success(response);
    }
}
