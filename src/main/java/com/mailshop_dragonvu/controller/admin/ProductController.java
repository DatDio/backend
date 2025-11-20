package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.products.ProductCreateDTO;
import com.mailshop_dragonvu.dto.products.ProductFilterDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.dto.products.ProductUpdateDTO;
import com.mailshop_dragonvu.service.ProductService;
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
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Product Management (Admin)", description = "Admin product management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    private final ProductService productService;

    @PostMapping("create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product", description = "Create a new product (Admin only)")
    public ApiResponse<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateDTO request) {
        ProductResponseDTO response = productService.createProduct(request);
        return ApiResponse.success("Product created successfully", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update product details (Admin only)")
    public ApiResponse<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDTO request) {
        ProductResponseDTO response = productService.updateProduct(id, request);
        return ApiResponse.success("Product updated successfully", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get product details by ID")
    public ApiResponse<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO response = productService.getProductById(id);
        return ApiResponse.success(response);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete product", description = "Soft delete (mark as deleted) a product")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success("Product deleted successfully");
    }


    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate product", description = "Activate a product")
    public ApiResponse<Void> activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return ApiResponse.success("Product activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate product", description = "Deactivate a product")
    public ApiResponse<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ApiResponse.success("Product deactivated successfully");
    }

}
