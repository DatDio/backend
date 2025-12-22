package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.products.ProductCreateDTO;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.dto.products.ProductUpdateDTO;
import com.mailshop_dragonvu.entity.CategoryEntity;
import com.mailshop_dragonvu.entity.ProductEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    // -------------------------
    // Convert Entity -> ResponseDTO
    // -------------------------
    public ProductResponseDTO toResponse(ProductEntity product, long quantity) {
        if (product == null) return null;

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .liveTime(product.getLiveTime())
                .country(product.getCountry())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .status(product.getStatus().getKey())
                .quantity(quantity)
                .sortOrder(product.getSortOrder())
                .build();
    }

    // -------------------------
    // Convert CreateDTO -> Entity
    // -------------------------
    public ProductEntity toEntity(ProductCreateDTO request, CategoryEntity category) {
        if (request == null) return null;

        return ProductEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
    }

    // -------------------------
    // Update Entity từ UpdateDTO
    // -------------------------
    public void updateEntity(ProductUpdateDTO request, ProductEntity product, CategoryEntity category) {
        if (request == null || product == null) return;

        if (request.getName() != null)
            product.setName(request.getName());

        if (request.getDescription() != null)
            product.setDescription(request.getDescription());

        if (request.getPrice() != null)
            product.setPrice(request.getPrice());

        if (request.getCategoryId() != null)
            product.setCategory(category);

        if (request.getStatus() != null)
            product.setStatus(ActiveStatusEnum.fromKey(request.getStatus()));

        if (request.getSortOrder() != null)
            product.setSortOrder(request.getSortOrder());
    }
}
