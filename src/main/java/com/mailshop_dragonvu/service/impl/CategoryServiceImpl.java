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
import org.springframework.util.StringUtils;

import java.util.Optional;

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
        if (categoryRepository.findByNameIgnoreCase(request.getName().trim()).isPresent()) {
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
            String newName = request.getName().trim();
            String currentName = category.getName().trim();

            if (!currentName.equalsIgnoreCase(newName) &&
                    categoryRepository.findByNameIgnoreCase(newName).isPresent()) {
                throw new BusinessException(CATEGORY_NAME_ALREADY_EXISTS);
            }
            category.setName(request.getName());
        }

        category.setDescription(request.getDescription());

        category.setStatus(ActiveStatusEnum.fromKey(request.getStatus()));

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
    public Page<CategoryResponseDTO> searchCategories(CategoryFilterDTO request) {

        Sort sort = Utils.generatedSort(request.getSort());

        Pageable pageable;

        if (request.getLimit() == null) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);
        } else {
            int page = Optional.ofNullable(request.getPage()).orElse(0);
            int limit = request.getLimit();
            pageable = PageRequest.of(page, limit, sort);
        }

        ActiveStatusEnum statusEnum = null;
        if (StringUtils.hasText(request.getStatus())) {
            try {
                statusEnum = ActiveStatusEnum.fromKey(
                        Integer.parseInt(request.getStatus())
                );
            } catch (NumberFormatException e) {
                throw new BusinessException("Lỗi convert enum");
            }
        }

        Page<CategoryEntity> categories = categoryRepository.searchCategories(
                request.getName(),
                statusEnum,
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
        CategoryEntity category = findCategoryOrThrow(id);
        categoryRepository.delete(category);
    }

    private CategoryEntity findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy danh mục"));
    }
}
