package com.mailshop_dragonvu.dto.orderitems;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;

    // Thông tin product
    private Long productId;
    private String productName;

    // Thông tin product item (tài khoản cụ thể)
    private Long productItemId;
    private String accountData;

    // Trạng thái bán
    private Boolean sold;
    private Long buyerId;
    private Long orderId;
    private LocalDateTime soldAt;

    private LocalDateTime createdAt;
}

