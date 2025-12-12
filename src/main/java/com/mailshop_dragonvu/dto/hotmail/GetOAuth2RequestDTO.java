package com.mailshop_dragonvu.dto.hotmail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for getting OAuth2 access tokens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOAuth2RequestDTO {
    /**
     * Email data in format: email|password|refresh_token|client_id
     * One email per line
     */
    private String emailData;
}
