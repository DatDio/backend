package com.mailshop_dragonvu.service;

/**
 * Casso Service Interface - VietQR Payment Integration
 * Handles QR code generation and webhook verification for Casso
 */
public interface CassoService {

    /**
     * Generate VietQR code URL for deposit
     * @param amount Deposit amount in VND
     * @param transactionCode Unique transaction code for matching
     * @return QR code URL from VietQR.io
     */
    String generateQRCodeUrl(Long amount, Long transactionCode);

    /**
     * Verify webhook request from Casso using secure-token header
     * @param secureToken The secure-token header from request
     * @return true if valid token
     */
    boolean verifyWebhook(String secureToken);

    /**
     * Get bank display name from bank code
     * @param bankCode Bank code (ACB, VCB, etc.)
     * @return Full bank name
     */
    String getBankName(String bankCode);
}
