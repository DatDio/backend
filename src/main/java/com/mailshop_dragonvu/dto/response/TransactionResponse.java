package com.mailshop_dragonvu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String transactionCode;
    private Long userId;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String status;
    private String description;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
