package com.mailshop_dragonvu.dto.casso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Casso Deposit Response DTO
 * Returned to frontend with QR code URL and transaction info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CassoDepositResponse {
    
    /**
     * VietQR image URL for displaying QR code
     */
    private String qrCodeUrl;
    
    /**
     * Unique transaction code (embedded in QR content)
     */
    private Long transactionCode;
    
    /**
     * Deposit amount
     */
    private Long amount;
    
    /**
     * Transfer content (user must include this when transferring)
     */
    private String transferContent;
    
    /**
     * Bank name for display
     */
    private String bankName;
    
    /**
     * Account number for display
     */
    private String accountNumber;
    
    /**
     * Account holder name for display
     */
    private String accountName;
}
