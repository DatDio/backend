package com.mailshop_dragonvu.dto.fpayment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FPayment Webhook DTO
 * Received from FPayment callback when payment status changes
 * 
 * Callback parameters (GET method):
 * - request_id: Internal transaction code
 * - trans_id: FPayment transaction ID
 * - merchant_id: Merchant ID for verification
 * - api_key: API key for verification
 * - received: Actual USDT amount received
 * - status: waiting, expired, completed
 * - from_address: USDT sender address (when completed)
 * - transaction_id: Blockchain transaction ID (when completed)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FPaymentWebhookDTO {
    
    /**
     * Internal transaction code (request_id sent when creating invoice)
     */
    private String requestId;
    
    /**
     * FPayment transaction ID
     */
    private String transId;
    
    /**
     * Merchant ID for verification
     */
    private String merchantId;
    
    /**
     * API key for verification
     */
    private String apiKey;
    
    /**
     * Amount in USDT from original invoice
     */
    private String amount;
    
    /**
     * Actual USDT amount received
     */
    private String received;
    
    /**
     * Transaction status: waiting, expired, completed
     */
    private String status;
    
    /**
     * USDT sender address (only when status = completed)
     */
    private String fromAddress;
    
    /**
     * Blockchain transaction ID (only when status = completed)
     */
    private String transactionId;
}
