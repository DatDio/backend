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
    private Long balance;
    private Long totalDeposited;
    private Long totalSpent;
    private Boolean isLocked;
    private String lockReason;
}
