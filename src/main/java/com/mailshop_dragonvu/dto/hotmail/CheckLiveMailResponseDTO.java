package com.mailshop_dragonvu.dto.hotmail;

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
    private boolean isLive;
    private String error;
}
