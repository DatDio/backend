package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.categories.CategoryCreateDTO;
import com.mailshop_dragonvu.dto.categories.CategoryFilterDTO;
import com.mailshop_dragonvu.dto.categories.CategoryResponseDTO;
import com.mailshop_dragonvu.dto.categories.CategoryUpdateDTO;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.service.CategoryService;
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

@RestController("adminCategoryController")
@RequestMapping("/admin/" + Constants.API_PATH.CATEGORIES)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Category Management (Admin)", description = "Admin category management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create category", description = "Create a new category (Admin only)")
    public ApiResponse<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryCreateDTO request) {
        log.info("Creating new category");
        CategoryResponseDTO response = categoryService.createCategory(request);
        return ApiResponse.success("Tạo danh mục thành công", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update category details (Admin only)")
    public ApiResponse<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateDTO request) {
        log.info("Updating category with ID: {}", id);
        CategoryResponseDTO response = categoryService.updateCategory(id, request);
        return ApiResponse.success("Category updated successfully", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Get category details by ID")
    public ApiResponse<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        log.info("Getting category with ID: {}", id);
        CategoryResponseDTO response = categoryService.getCategoryById(id);
        return ApiResponse.success(response);
    }


    @GetMapping("/status/{status}/count")
    @Operation(summary = "Count categories by status", description = "Get count of categories by status")
    public ApiResponse<Integer> countByStatus(@PathVariable ActiveStatusEnum status) {
        Integer count = categoryService.countByStatus(status);
        return ApiResponse.success(count);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category (Admin only)")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        log.info("Deleting category with ID: {}", id);
        categoryService.deleteCategory(id);
        return ApiResponse.success("Category deleted successfully");
    }
}
