package com.mailshop_dragonvu.dto.orders;

import com.mailshop_dragonvu.dto.orderitems.OrderItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private Long userId;
    private String userEmail;
    private String orderStatus;
    private Long totalAmount;
    // Thông tin product
    private Long productId;
    private String productName;

    private List<String> accountData;

    private LocalDateTime createdAt;

}
