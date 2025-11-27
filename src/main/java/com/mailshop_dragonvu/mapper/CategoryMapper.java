package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.categories.CategoryCreateDTO;
import com.mailshop_dragonvu.dto.categories.CategoryResponseDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.entity.CategoryEntity;
import com.mailshop_dragonvu.entity.ProductEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.repository.ProductItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    private final ProductItemRepository productItemRepository;

    @Autowired
    public CategoryMapper(ProductItemRepository productItemRepository) {
        this.productItemRepository = productItemRepository;
    }

    public CategoryResponseDTO toResponse(CategoryEntity entity) {
        if (entity == null) return null;

        List<ProductResponseDTO> productResponses = entity.getProducts().stream()
                .filter(p -> p.getStatus() == ActiveStatusEnum.ACTIVE)
                .map(p -> {
                    long quantity = productItemRepository.countAvailableItems(p.getId());

                    return ProductResponseDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .liveTime(p.getLiveTime())
                            .country(p.getCountry())
                            .description(p.getDescription())
                            .price(p.getPrice())
                            .quantity(quantity)
                            .build();
                })
                .collect(Collectors.toList());

        return CategoryResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus().getKey())
                .createdAt(entity.getCreatedAt())
                .products(productResponses)
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
