package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.response.PayOSPaymentResponse;

import java.math.BigDecimal;
import java.util.Map;

/**
 * PayOS Service Interface - PayOS Payment Gateway Integration
 */
public interface PayOSService {

    /**
     * Create PayOS payment link with QR code
     */
    PayOSPaymentResponse createPaymentLink(Long orderCode, BigDecimal amount, String description, String returnUrl, String cancelUrl);

    /**
     * Get payment status from PayOS
     */
    String getPaymentStatus(Long orderCode);

    /**
     * Verify webhook signature from PayOS
     */
    boolean verifyWebhookSignature(Map<String, String> webhookData, String signature);

    /**
     * Cancel payment
     */
    void cancelPayment(Long orderCode);
}
