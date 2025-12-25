package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.products.ProductCreateDTO;
import com.mailshop_dragonvu.dto.products.ProductFilterDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.dto.products.ProductUpdateDTO;
import com.mailshop_dragonvu.service.ProductService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController("adminProductController")
@RequestMapping("/admin/" + Constants.API_PATH.PRODUCTS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    private final ProductService productService;

    @PostMapping(value = "create", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponseDTO> createProduct(@Valid @ModelAttribute ProductCreateDTO request) {
        ProductResponseDTO response = productService.createProduct(request);
        return ApiResponse.success("Product created successfully", response);
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ApiResponse<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductUpdateDTO request) {
        ProductResponseDTO response = productService.updateProduct(id, request);
        return ApiResponse.success("Product updated successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO response = productService.getProductById(id);
        return ApiResponse.success(response);
    }


    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success("Product deleted successfully");
    }


    @PatchMapping("/{id}/activate")
    public ApiResponse<Void> activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return ApiResponse.success("Product activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ApiResponse.success("Product deactivated successfully");
    }

}
