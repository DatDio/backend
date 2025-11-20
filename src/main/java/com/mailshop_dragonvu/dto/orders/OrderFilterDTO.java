package com.mailshop_dragonvu.dto.orders;

import com.mailshop_dragonvu.dto.PageFilterDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderFilterDTO extends PageFilterDTO {
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
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
