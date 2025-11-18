package com.mailshop_dragonvu.dto.wallets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Wallet Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    private Long id;
    private Long userId;
    private BigDecimal balance;
    private BigDecimal totalDeposited;
    private BigDecimal totalSpent;
    private Boolean isLocked;
    private String lockReason;
}
