package com.mailshop_dragonvu.dto.web2m;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Web2m Transaction DTO
 * Represents a single transaction from Web2m webhook
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Web2mTransactionDTO {
    
    /**
     * Web2m internal transaction ID
     */
    private String id;
    
    /**
     * Transaction type (e.g., "TIN" for incoming)
     */
    private String type;
    
    /**
     * Bank transaction ID
     */
    private String transactionId;
    
    /**
     * Transfer amount (as string from webhook)
     */
    private String amount;
    
    /**
     * Transfer content/description - CRITICAL for matching
     */
    private String description;
    
    /**
     * Transaction date (format: yyyy-MM-dd HH:mm:ss)
     */
    private String date;
    
    /**
     * Sender name
     */
    private String name;
    
    /**
     * Parse amount as Long
     */
    public Long getAmountAsLong() {
        try {
            return Long.parseLong(amount.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
