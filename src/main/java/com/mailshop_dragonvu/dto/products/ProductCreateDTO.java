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

    // Cấu hình kho phụ (Secondary Warehouse)
    private Integer minSecondaryStock;  // Mặc định: 500
    private Integer maxSecondaryStock;  // Mặc định: 1000

    // Thời gian hết hạn (giờ, 0 = không hết hạn)
    private Integer expirationHours;  // Mặc định: 0
}
