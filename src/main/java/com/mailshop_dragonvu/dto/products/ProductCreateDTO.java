package com.mailshop_dragonvu.dto.products;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateDTO {

    @NotBlank
    private String name;

    private String description;

    private String liveTime;

    private String country;

    @NotNull
    private Long price;

    @NotNull
    private Long categoryId;

    private MultipartFile image;

    private Integer sortOrder;
}
