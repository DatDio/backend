package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.fpayment.FPaymentApiResponse;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.service.FPaymentProvider;
import com.mailshop_dragonvu.service.FPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * FPayment Service Implementation
 * Handles FPayment API calls for crypto payment processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FPaymentServiceImpl implements FPaymentService {

    private final FPaymentProvider fpaymentProvider;
    private final RestTemplate restTemplate;

    @Override
    public FPaymentApiResponse createInvoice(
            String name,
            String description,
            BigDecimal amountUsdt,
            String requestId,
            String callbackUrl,
            String successUrl,
            String cancelUrl) {

        fpaymentProvider.validateConfiguration();

        // Build request parameters
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchant_id", fpaymentProvider.getMerchantId());
        params.add("api_key", fpaymentProvider.getApiKey());
        params.add("name", name);
        params.add("description", description);
        params.add("amount", amountUsdt.setScale(3, RoundingMode.CEILING).toString());
        params.add("request_id", requestId);
        params.add("callback_url", callbackUrl);
        params.add("success_url", successUrl);
        params.add("cancel_url", cancelUrl);

        // Set headers for form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            log.info("Creating FPayment invoice - requestId: {}, amountUsdt: {}", requestId, amountUsdt);

            ResponseEntity<FPaymentApiResponse> response = restTemplate.exchange(
                    FPaymentProvider.API_ADD_INVOICE,
                    HttpMethod.POST,
                    request,
                    FPaymentApiResponse.class
            );

            FPaymentApiResponse apiResponse = response.getBody();
            if (apiResponse == null) {
                log.error("Empty response from FPayment API");
                throw new BusinessException(ErrorCode.PAYMENT_CREATION_FAILED);
            }

            if (!apiResponse.isSuccess()) {
                log.error("FPayment API error: {}", apiResponse.getMsg());
                throw new BusinessException(ErrorCode.PAYMENT_CREATION_FAILED);
            }

            log.info("FPayment invoice created - transId: {}, urlPayment: {}",
                    apiResponse.getData().getTransId(),
                    apiResponse.getData().getUrlPayment());

            return apiResponse;

        } catch (RestClientException e) {
            log.error("Failed to call FPayment API: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.PAYMENT_CREATION_FAILED);
        }
    }

    @Override
    public FPaymentApiResponse getInvoiceStatus(String transId) {
        fpaymentProvider.validateConfiguration();

        // Build request parameters
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchant_id", fpaymentProvider.getMerchantId());
        params.add("api_key", fpaymentProvider.getApiKey());
        params.add("trans_id", transId);

        // Set headers for form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            log.info("Getting FPayment invoice status - transId: {}", transId);

            ResponseEntity<FPaymentApiResponse> response = restTemplate.exchange(
                    FPaymentProvider.API_GET_INVOICE_STATUS,
                    HttpMethod.POST,
                    request,
                    FPaymentApiResponse.class
            );

            FPaymentApiResponse apiResponse = response.getBody();
            if (apiResponse == null) {
                log.error("Empty response from FPayment API");
                throw new BusinessException(ErrorCode.PAYMENT_CREATION_FAILED);
            }

            log.info("FPayment invoice status - transId: {}, status: {}",
                    transId,
                    apiResponse.getData() != null ? apiResponse.getData().getStatus() : "unknown");

            return apiResponse;

        } catch (RestClientException e) {
            log.error("Failed to get FPayment invoice status: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.PAYMENT_CREATION_FAILED);
        }
    }

    @Override
    public boolean verifyWebhook(String merchantId, String apiKey) {
        return fpaymentProvider.verifyCredentials(merchantId, apiKey);
    }

    @Override
    public BigDecimal convertVndToUsdt(Long amountVnd) {
        if (amountVnd == null || amountVnd <= 0) {
            return BigDecimal.ZERO;
        }
        Long rate = fpaymentProvider.getUsdVndRate();
        // VND / rate = USDT, rounded up to 3 decimal places
        return BigDecimal.valueOf(amountVnd)
                .divide(BigDecimal.valueOf(rate), 3, RoundingMode.CEILING);
    }

    @Override
    public Long convertUsdtToVnd(BigDecimal amountUsdt) {
        if (amountUsdt == null || amountUsdt.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }
        Long rate = fpaymentProvider.getUsdVndRate();
        // USDT * rate = VND
        return amountUsdt.multiply(BigDecimal.valueOf(rate)).longValue();
    }
}
