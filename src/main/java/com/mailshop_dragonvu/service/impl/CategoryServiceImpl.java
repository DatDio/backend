package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.categories.CategoryCreateDTO;
import com.mailshop_dragonvu.dto.categories.CategoryFilterDTO;
import com.mailshop_dragonvu.dto.categories.CategoryResponseDTO;
import com.mailshop_dragonvu.dto.categories.CategoryUpdateDTO;
import com.mailshop_dragonvu.entity.CategoryEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.CategoryMapper;
import com.mailshop_dragonvu.repository.CategoryRepository;
import com.mailshop_dragonvu.service.CategoryService;
import com.mailshop_dragonvu.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final String CATEGORY_NAME_ALREADY_EXISTS = "Danh mục đã tồn tại";
    @Override
    public CategoryResponseDTO createCategory(CategoryCreateDTO request) {

        // Check if category with same name already exists
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException(CATEGORY_NAME_ALREADY_EXISTS);
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(ActiveStatusEnum.ACTIVE)
                .build();

        CategoryEntity savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO request) {

        CategoryEntity category = findCategoryOrThrow(id);

        if (request.getName() != null) {
            // Check if new name already exists (and not same as current)
            if (!category.getName().equals(request.getName()) &&
                categoryRepository.findByName(request.getName()).isPresent()) {
                throw new BusinessException(CATEGORY_NAME_ALREADY_EXISTS);
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        CategoryEntity updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", id);

        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        log.info("Getting category by ID: {}", id);
        CategoryEntity category = findCategoryOrThrow(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> searchCategories(CategoryFilterDTO categoryFilterRequest) {
        Sort sort = Utils.generatedSort(categoryFilterRequest.getSort());
        Pageable pageable = PageRequest.of(categoryFilterRequest.getPage(), categoryFilterRequest.getLimit(), sort);
        Page<CategoryEntity> categories = categoryRepository.searchCategories(
                categoryFilterRequest.getName(),
                categoryFilterRequest.getStatus(),
                pageable);
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countByStatus(ActiveStatusEnum status) {
        Integer count = categoryRepository.countByStatus(status);
        return count != null ? count : 0;
    }

    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);
        CategoryEntity category = findCategoryOrThrow(id);
        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }

    private CategoryEntity findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy danh mục"));
    }
}
