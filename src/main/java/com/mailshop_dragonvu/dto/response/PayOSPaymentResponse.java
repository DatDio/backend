package com.mailshop_dragonvu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayOS Payment Response - Contains QR code and payment link
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSPaymentResponse {

    private String transactionCode;
    private Long orderCode;
    private String paymentUrl;
    private String qrCode;
    private String checkoutUrl;
    private String amount;
    private String description;
    private String status;
}
