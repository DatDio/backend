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
}
