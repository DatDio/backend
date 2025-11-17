package com.mailshop_dragonvu.service;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentProviderService {

    String createPayment(String paymentNumber, BigDecimal amount, String currency, 
                        String orderId, String returnUrl, String cancelUrl);

    boolean verifyCallback(Map<String, String> callbackData);

    String getTransactionId(Map<String, String> callbackData);

    boolean isPaymentSuccessful(Map<String, String> callbackData);

    String refundPayment(String transactionId, BigDecimal amount);

}
