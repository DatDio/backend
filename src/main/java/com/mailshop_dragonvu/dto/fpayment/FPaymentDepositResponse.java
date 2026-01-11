package com.mailshop_dragonvu.dto.fpayment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * FPayment Deposit Response DTO
 * Returned to frontend with payment URL and transaction info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FPaymentDepositResponse {
    
    /**
     * FPayment payment URL - redirect user to this URL
     */
    private String urlPayment;
    
    /**
     * FPayment transaction ID
     */
    private String transId;
    
    /**
     * Amount in USDT to pay
     */
    private BigDecimal amountUsdt;
    
    /**
     * Amount in VND that will be added after successful payment
     */
    private Long amountVnd;
    
    /**
     * Internal transaction code for tracking
     */
    private Long transactionCode;
    
    /**
     * USD to VND exchange rate used
     */
    private Long exchangeRate;
}
