package com.mailshop_dragonvu.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket message for deposit success notification
 * Sent to user-specific topic when deposit is confirmed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositSuccessMessage {
    
    /**
     * User ID who made the deposit
     */
    private Long userId;
    
    /**
     * Transaction code
     */
    private Long transactionCode;
    
    /**
     * Deposit amount
     */
    private Long amount;
    
    /**
     * Bonus amount (if any)
     */
    private Long bonusAmount;
    
    /**
     * Total amount added to wallet
     */
    private Long totalAmount;
    
    /**
     * New wallet balance after deposit
     */
    private Long newBalance;
    
    /**
     * Success message
     */
    private String message;
}
