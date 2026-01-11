package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.fpayment.FPaymentApiResponse;

import java.math.BigDecimal;

/**
 * FPayment Service Interface - Crypto Payment Integration
 * Handles invoice creation and status checking for FPayment
 */
public interface FPaymentService {

    /**
     * Create a new invoice on FPayment
     * @param name Invoice name (e.g., "Nạp tiền vào ví")
     * @param description Invoice description (e.g., user email)
     * @param amountUsdt Amount in USDT
     * @param requestId Internal transaction code for matching
     * @param callbackUrl URL for receiving payment status
     * @param successUrl URL to redirect after successful payment
     * @param cancelUrl URL to redirect after failed/expired payment
     * @return FPayment API response with payment URL
     */
    FPaymentApiResponse createInvoice(
            String name,
            String description,
            BigDecimal amountUsdt,
            String requestId,
            String callbackUrl,
            String successUrl,
            String cancelUrl
    );

    /**
     * Get invoice status from FPayment
     * @param transId FPayment transaction ID
     * @return FPayment API response with current status
     */
    FPaymentApiResponse getInvoiceStatus(String transId);

    /**
     * Verify webhook request from FPayment
     * @param merchantId Merchant ID from webhook
     * @param apiKey API Key from webhook
     * @return true if credentials are valid
     */
    boolean verifyWebhook(String merchantId, String apiKey);

    /**
     * Convert VND to USDT based on configured exchange rate
     * @param amountVnd Amount in VND
     * @return Amount in USDT (rounded to 3 decimal places)
     */
    BigDecimal convertVndToUsdt(Long amountVnd);

    /**
     * Convert USDT to VND based on configured exchange rate
     * @param amountUsdt Amount in USDT
     * @return Amount in VND
     */
    Long convertUsdtToVnd(BigDecimal amountUsdt);
}
