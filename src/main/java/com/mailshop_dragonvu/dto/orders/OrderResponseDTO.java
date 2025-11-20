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
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String phone;
    private String email;
    private String notes;
    private String cancellationReason;
    private List<OrderItemResponse> orderItems;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
