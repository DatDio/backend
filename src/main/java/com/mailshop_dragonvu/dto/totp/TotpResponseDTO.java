package com.mailshop_dragonvu.dto.totp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for single TOTP 2FA code result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotpResponseDTO {

    /**
     * Display identifier (secret truncated or full if short)
     */
    private String identifier;

    /**
     * Original secret (for reference)
     */
    private String secret;

    /**
     * Generated 6-digit TOTP code
     */
    private String code;

    /**
     * Status: success or error
     */
    private String status;

    /**
     * Seconds remaining until code expires (0-30)
     */
    private int timeRemaining;

    /**
     * Error message if status is error
     */
    private String error;
}
