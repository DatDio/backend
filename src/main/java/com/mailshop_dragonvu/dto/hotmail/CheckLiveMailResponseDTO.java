package com.mailshop_dragonvu.dto.hotmail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for check live mail result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckLiveMailResponseDTO {
    private String email;
    private String password;
    private String refreshToken;
    private String clientId;
    
    @JsonProperty("isLive")
    private boolean isLive;
    
    private CheckStatus status;
    private String error;
}

