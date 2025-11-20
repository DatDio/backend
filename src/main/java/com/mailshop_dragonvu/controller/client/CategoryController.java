package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.categories.CategoryFilterDTO;
import com.mailshop_dragonvu.dto.categories.CategoryResponseDTO;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.service.CategoryService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController("clientCategoryController")
@RequestMapping(Constants.API_PATH.CATEGORIES)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management (Client)", description = "Client category browsing APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Get category details by ID")
    public ApiResponse<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        log.info("Getting category with ID: {}", id);
        CategoryResponseDTO response = categoryService.getCategoryById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Search categories with dynamic filters")
    public ApiResponse<Page<CategoryResponseDTO>> searchCategories( CategoryFilterDTO categoryFilterDTO) {
        Page<CategoryResponseDTO> response = categoryService.searchCategories(categoryFilterDTO);
        return ApiResponse.success(response);
    }

}
