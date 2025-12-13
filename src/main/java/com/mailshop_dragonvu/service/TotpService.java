package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.totp.TotpResponseDTO;

import java.util.List;

/**
 * Service interface for TOTP 2FA code generation
 */
public interface TotpService {

    /**
     * Generate TOTP codes for multiple secrets
     *
     * @param secretData Raw secret data (one secret per line)
     * @return List of TOTP results for each secret
     */
    List<TotpResponseDTO> generateCodes(String secretData);

    /**
     * Generate TOTP code for a single secret
     *
     * @param secret The TOTP secret key (Base32 encoded)
     * @return Generated 6-digit code or null if invalid
     */
    String generateCode(String secret);

    /**
     * Get time remaining until current TOTP code expires
     *
     * @return Seconds remaining (0-30)
     */
    int getTimeRemaining();
}
