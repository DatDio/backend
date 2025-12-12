package com.mailshop_dragonvu.dto.facebook;

import com.mailshop_dragonvu.dto.hotmail.CheckStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Facebook check live result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookCheckLiveResponseDTO {
    private String uid;
    private CheckStatus status;
    private String avatar;
    private String error;
}
