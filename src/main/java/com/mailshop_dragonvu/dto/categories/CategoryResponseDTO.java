package com.mailshop_dragonvu.dto.categories;

import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private List<ProductResponseDTO> products;
}
