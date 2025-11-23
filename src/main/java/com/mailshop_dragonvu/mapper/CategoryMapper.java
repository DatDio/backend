package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.categories.CategoryCreateDTO;
import com.mailshop_dragonvu.dto.categories.CategoryResponseDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.entity.CategoryEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponse(CategoryEntity entity) {
        if (entity == null) return null;

        return CategoryResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus().getValue())
                .products(
                        entity.getProducts().stream()
                                .filter(p -> p.getStatus() == ActiveStatusEnum.ACTIVE)
                                .map(p -> ProductResponseDTO.builder()
                                        .id(p.getId())
                                        .name(p.getName())
                                        .description(p.getDescription())
                                        .price(p.getPrice())
                                        .build())
                                .toList()
                )
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
