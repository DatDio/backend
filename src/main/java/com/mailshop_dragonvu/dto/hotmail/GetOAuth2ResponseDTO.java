package com.mailshop_dragonvu.dto.hotmail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for renew refresh token result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOAuth2ResponseDTO {
    private String email;
    private String password;
    private String refreshToken;      // New refresh token after renewal
    private String clientId;
    private String accessToken;
    private String fullData;          // Complete data string: email|pass|newToken|clientId
    private boolean success;
    private CheckStatus status;
    private String error;
}

