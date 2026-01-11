package com.mailshop_dragonvu.dto.fpayment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FPayment API Response DTO
 * Used to parse response from FPayment AddInvoice and GetInvoiceStatus APIs
 * 
 * Sample response:
 * {
 *   "data": {
 *     "trans_id": "TX2604571052543841",
 *     "amount": "12.001",
 *     "status": "waiting",
 *     "url_payment": "https://app.fpayment.net/payment/TX2604571052543841"
 *   },
 *   "status": "success",
 *   "msg": "Invoice creation successful!"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FPaymentApiResponse {
    
    /**
     * Response status: "success" or "error"
     */
    private String status;
    
    /**
     * Message from API
     */
    private String msg;
    
    /**
     * Response data
     */
    private FPaymentData data;
    
    /**
     * Nested data class for invoice information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FPaymentData {
        
        /**
         * FPayment transaction ID
         */
        @JsonProperty("trans_id")
        private String transId;
        
        /**
         * Internal request ID (for GetInvoiceStatus)
         */
        @JsonProperty("request_id")
        private String requestId;
        
        /**
         * Amount in USDT
         */
        private String amount;
        
        /**
         * Actual received amount (for GetInvoiceStatus)
         */
        private String received;
        
        /**
         * Transaction status: waiting, completed, expired
         */
        private String status;
        
        /**
         * Payment URL for user to complete payment
         */
        @JsonProperty("url_payment")
        private String urlPayment;
        
        /**
         * USDT sender address (when completed)
         */
        @JsonProperty("from_address")
        private String fromAddress;
        
        /**
         * Blockchain transaction ID (when completed)
         */
        @JsonProperty("transaction_id")
        private String transactionId;
        
        /**
         * Invoice creation time
         */
        @JsonProperty("create_gettime")
        private String createTime;
        
        /**
         * Last update time
         */
        @JsonProperty("update_gettime")
        private String updateTime;
    }
    
    /**
     * Check if response is successful
     */
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}
