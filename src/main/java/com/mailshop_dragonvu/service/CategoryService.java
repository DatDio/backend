package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.categories.CategoryCreateDTO;
import com.mailshop_dragonvu.dto.categories.CategoryFilterDTO;
import com.mailshop_dragonvu.dto.categories.CategoryResponseDTO;
import com.mailshop_dragonvu.dto.categories.CategoryUpdateDTO;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import org.springframework.data.domain.Page;

public interface CategoryService {

    // Create
    CategoryResponseDTO createCategory(CategoryCreateDTO request);

    // Update
    CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO request);

    // Get by ID
    CategoryResponseDTO getCategoryById(Long id);


    // Search with dynamic filters
    Page<CategoryResponseDTO> searchCategories(CategoryFilterDTO request);

    // Count by status
    Integer countByStatus(ActiveStatusEnum status);

    // Delete
    void deleteCategory(Long id);
}
