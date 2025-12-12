package com.mailshop_dragonvu.dto.hotmail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Hotmail get code API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HotmailGetCodeResponseDTO {

    /**
     * Email address
     */
    private String email;

    /**
     * Password (from input data)
     */
    private String password;
    
    /**
     * Refresh token (from input data)
     */
    private String refreshToken;
    
    /**
     * Client ID (from input data)
     */
    private String clientId;

    /**
     * Whether code was found successfully
     */
    @Builder.Default
    private boolean status = false;
    
    /**
     * Status category for 3-column display
     */
    private CheckStatus checkStatus;

    /**
     * Extracted verification code
     */
    private String code;

    /**
     * Full message content/subject
     */
    private String content;

    /**
     * Time when code was received (formatted string)
     */
    private String date;
}

