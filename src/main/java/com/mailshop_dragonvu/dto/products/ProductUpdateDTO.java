package com.mailshop_dragonvu.dto.products;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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

    private MultipartFile image;

    private Integer sortOrder;

    // Cấu hình kho phụ (Secondary Warehouse)
    private Integer minSecondaryStock;
    private Integer maxSecondaryStock;

    // Thời gian hết hạn (giờ, 0 = không hết hạn)
    private Integer expirationHours;
}
