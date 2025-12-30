package com.mailshop_dragonvu.dto.web2m;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Web2m Webhook Request DTO
 * Received from Web2m callback when bank transfer is detected
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Web2mWebhookDTO {
    
    private Boolean status;
    private List<Web2mTransactionDTO> data;
}
