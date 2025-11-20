package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.categories.CategoryCreateDTO;
import com.mailshop_dragonvu.dto.categories.CategoryResponseDTO;
import com.mailshop_dragonvu.entity.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponse(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return CategoryResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .build();
    }

    public CategoryEntity toEntity(CategoryCreateDTO request) {
        if (request == null) {
            return null;
        }

        return CategoryEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }
}
