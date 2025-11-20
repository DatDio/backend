package com.mailshop_dragonvu.dto.products;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateDTO {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long price;

    @NotNull
    private Long categoryId;
}
