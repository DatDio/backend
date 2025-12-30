package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.web2m.Web2mDepositResponse;
import com.mailshop_dragonvu.dto.web2m.Web2mWebhookDTO;

/**
 * Web2m Service Interface - VietQR Payment Integration
 * Handles QR code generation and webhook verification
 */
public interface Web2mService {

    /**
     * Generate VietQR code URL for deposit
     * @param amount Deposit amount in VND
     * @param transactionCode Unique transaction code for matching
     * @return QR code URL from VietQR.io
     */
    String generateQRCodeUrl(Long amount, Long transactionCode);

    /**
     * Verify webhook request from Web2m
     * @param authorizationHeader The Authorization header from request
     * @return true if valid bearer token
     */
    boolean verifyWebhook(String authorizationHeader);

    /**
     * Get bank display name from bank code
     * @param bankCode Bank code (ACB, VCB, etc.)
     * @return Full bank name
     */
    String getBankName(String bankCode);
}
