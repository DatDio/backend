package com.mailshop_dragonvu.dto.categories;

import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String status;

    private List<ProductResponseDTO> products;
}
