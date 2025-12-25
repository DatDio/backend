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

    // Số lượng còn lại trong kho PHỤ (hiển thị cho khách hàng)
    private Long quantity;

    // Số lượng còn lại trong kho CHÍNH (chỉ hiển thị cho admin)
    private Long primaryQuantity;

    // Cấu hình kho phụ
    private Integer minSecondaryStock;
    private Integer maxSecondaryStock;

    private Integer sortOrder;

    // Thời gian hết hạn của mail (giờ, 0 = không hết hạn)
    private Integer expirationHours;
}
