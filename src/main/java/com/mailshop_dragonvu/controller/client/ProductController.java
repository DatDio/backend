package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.products.ProductFilterDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseClientDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.service.ProductService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ApiResponse<List<ProductResponseClientDTO>> getAllActiveProducts() {
        List<ProductResponseClientDTO> response = productService.getAllActiveProducts();
        return ApiResponse.success(response);
    }
}
