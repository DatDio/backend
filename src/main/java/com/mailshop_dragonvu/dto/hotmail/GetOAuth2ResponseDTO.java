package com.mailshop_dragonvu.dto.hotmail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for get OAuth2 token result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOAuth2ResponseDTO {
    private String email;
    private String password;
    private String refreshToken;
    private String clientId;
    private String accessToken;
    private boolean success;
    private CheckStatus status;
    private String error;
}
