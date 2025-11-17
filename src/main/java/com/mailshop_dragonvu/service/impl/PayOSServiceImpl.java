package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.response.PayOSPaymentResponse;
import com.mailshop_dragonvu.service.PayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * PayOS Service Implementation
 * Integration with PayOS Payment Gateway for Vietnamese market
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSServiceImpl implements PayOSService {

    private final RestTemplate restTemplate;

    @Value("${payos.api.url:https://api-merchant.payos.vn}")
    private String payosApiUrl;

    @Value("${payos.client.id}")
    private String clientId;

    @Value("${payos.api.key}")
    private String apiKey;

    @Value("${payos.checksum.key}")
    private String checksumKey;

    @Override
    public PayOSPaymentResponse createPaymentLink(Long orderCode, BigDecimal amount, 
                                                  String description, String returnUrl, String cancelUrl) {
        log.info("Creating PayOS payment link for order: {}, amount: {}", orderCode, amount);

        try {
            // Prepare request body for PayOS API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderCode", orderCode);
            requestBody.put("amount", amount.intValue()); // PayOS requires integer (VND)
            requestBody.put("description", description);
            requestBody.put("returnUrl", returnUrl);
            requestBody.put("cancelUrl", cancelUrl);

            // Generate checksum for security
            String checksum = generateChecksum(orderCode, amount.intValue());
            requestBody.put("signature", checksum);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Call PayOS API
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    payosApiUrl + "/v2/payment-requests",
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
                
                return PayOSPaymentResponse.builder()
                        .transactionCode("TXN" + orderCode)
                        .orderCode(orderCode)
                        .paymentUrl((String) responseData.get("paymentLinkId"))
                        .qrCode((String) responseData.get("qrCode"))
                        .checkoutUrl((String) responseData.get("checkoutUrl"))
                        .amount(amount.toString())
                        .description(description)
                        .status("PENDING")
                        .build();
            }

            throw new RuntimeException("Failed to create PayOS payment link");

        } catch (Exception e) {
            log.error("Error creating PayOS payment: {}", e.getMessage(), e);
            throw new RuntimeException("PayOS payment creation failed: " + e.getMessage());
        }
    }

    @Override
    public String getPaymentStatus(Long orderCode) {
        log.info("Getting payment status for order: {}", orderCode);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    payosApiUrl + "/v2/payment-requests/" + orderCode,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
                return (String) responseData.get("status");
            }

            return "UNKNOWN";

        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            return "ERROR";
        }
    }

    @Override
    public boolean verifyWebhookSignature(Map<String, String> webhookData, String receivedSignature) {
        try {
            String data = webhookData.get("orderCode") + 
                         webhookData.get("amount") + 
                         webhookData.get("description") + 
                         webhookData.get("status");
            
            String calculatedSignature = generateHmacSHA256(data, checksumKey);
            
            return calculatedSignature.equals(receivedSignature);

        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void cancelPayment(Long orderCode) {
        log.info("Cancelling payment for order: {}", orderCode);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cancellationReason", "User cancelled");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            restTemplate.postForEntity(
                    payosApiUrl + "/v2/payment-requests/" + orderCode + "/cancel",
                    request,
                    Map.class
            );

            log.info("Payment cancelled successfully for order: {}", orderCode);

        } catch (Exception e) {
            log.error("Error cancelling payment: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate checksum for request security
     */
    private String generateChecksum(Long orderCode, int amount) {
        String data = orderCode + ":" + amount;
        return generateHmacSHA256(data, checksumKey);
    }

    /**
     * Generate HMAC SHA256 signature
     */
    private String generateHmacSHA256(String data, String key) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA256", e);
        }
    }
}
