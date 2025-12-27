package com.mailshop_dragonvu.dto.productitems;

import lombok.Data;

@Data
public class ProductItemCreateDTO {
    private Long productId;

    private String accountData;
    
    // Số giờ hết hạn (0 hoặc null = không hết hạn) - DEPRECATED, dùng expirationType
    // Options: 3 (3h), 6 (6h), 720 (1 tháng), 4320 (6 tháng)
    private Integer expirationHours;
    
    // ExpirationType enum: NONE, HOURS_3, HOURS_6, MONTH_1, MONTH_6
    private String expirationType;
}
