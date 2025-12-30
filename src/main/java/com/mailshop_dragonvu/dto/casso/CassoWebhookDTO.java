package com.mailshop_dragonvu.dto.casso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Casso Webhook Request DTO
 * Received from Casso callback when bank transfer is detected
 * 
 * Casso sends: { "error": 0, "data": [...] } (array of transactions)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CassoWebhookDTO {
    
    /**
     * Error code (0 = success)
     */
    private Integer error;
    
    /**
     * Transaction data (array of transactions)
     */
    private List<CassoTransactionDTO> data;
}
