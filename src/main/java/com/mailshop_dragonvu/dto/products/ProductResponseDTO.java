package com.mailshop_dragonvu.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private Long id;

    private String name;

    private String description;

    private Long price;

    private String liveTime;

    private String country;

    private String imageUrl;

    private Long categoryId;
    private String categoryName;

    private Integer status;

    // Số lượng còn lại (đếm từ ProductItem)
    private Long quantity;

    private Integer sortOrder;
}
