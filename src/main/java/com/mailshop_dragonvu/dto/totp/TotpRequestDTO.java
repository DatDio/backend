package com.mailshop_dragonvu.dto.totp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for TOTP 2FA code generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotpRequestDTO {

    /**
     * Secret data for 2FA generation
     * Format: Just secret key per line
     * Multiple lines supported (one secret per line)
     */
    private String secretData;
}
