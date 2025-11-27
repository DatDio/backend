package com.mailshop_dragonvu.dto.products;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDTO {
    @NotNull
    private Long id;

    private String name;

    private String description;

    private Long price;

    private Long categoryId;

    private Integer status;

    private String liveTime;

    private String country;
}
