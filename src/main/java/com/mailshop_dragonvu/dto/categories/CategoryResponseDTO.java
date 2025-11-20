package com.mailshop_dragonvu.dto.categories;

import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String description;
    private ActiveStatusEnum status;
}
