package com.mailshop_dragonvu.dto.productitems;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductItemResponseDTO {
    private Long id;
    private Long productId;

    private String accountData;   // mail|pass|recovery
    private Boolean sold;
    private String warehouseType; // PRIMARY or SECONDARY

    private Long buyerId;         // null nếu chưa bán
    private String buyerName;
    private Long orderId;         // null nếu chưa bán
    private String soldAt;
    
    // Thông tin hết hạn (per item)
    private String createdAt;         // Thời gian upload
    private String expirationType;    // Loại: NONE, HOURS_3, HOURS_6, MONTH_1, MONTH_6
    private String expirationLabel;   // Label hiển thị: "3 giờ", "6 giờ", "1 tháng", "6 tháng", "Không hết hạn"
    private String expiresAt;         // Thời điểm hết hạn (null = không hết hạn)
    private Boolean expired;          // Đã bị đánh dấu hết hạn
    private String expiredAt;         // Thời gian bị đánh dấu hết hạn
}



