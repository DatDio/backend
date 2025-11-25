package com.mailshop_dragonvu.dto.products;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private Long id;

    private String name;

    private String description;

    private Long price;

    private Long categoryId;
    private String categoryName;

    private Integer status;

    // Số lượng còn lại (đếm từ ProductItem)
    private Long quantity;
}
