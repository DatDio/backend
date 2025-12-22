package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.service.PayOSProvider;
import com.mailshop_dragonvu.service.PayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.ConfirmWebhookResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSServiceImpl implements PayOSService {

    private final PayOSProvider payOSProvider;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @Override
    public CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest createPaymentLinkRequest) {
        try {
            // Get PayOS instance with current configuration from database
            PayOS payOS = payOSProvider.getPayOS();

            final String description = createPaymentLinkRequest.getDescription();

            long orderCode = System.currentTimeMillis() / 1000;

            CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(createPaymentLinkRequest.getAmount())
                    .description(description != null ? description : "NAPTIEN")
                    .cancelUrl(cancelUrl)
                    .returnUrl(returnUrl)
                    .build();

            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentRequest);
            return data;
        } catch (BusinessException e) {
            throw e; // Re-throw business exceptions (like PAYMENT_CONFIGURATION_ERROR)
        } catch (Exception e) {
            log.error("PayOS create link failed", e);
            throw new BusinessException(ErrorCode.PAYMENT_CREATION_FAILED);
        }
    }

    @Override
    public WebhookData verifyWebhook(Webhook webhook) {
        try {
            PayOS payOS = payOSProvider.getPayOS();
            WebhookData data = payOS.webhooks().verify(webhook);
            if (!"00".equals(data.getCode())) {
                throw new BusinessException(ErrorCode.INVALID_WEBHOOK);
            }
            return data;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK);
        }
    }

    @Override
    public void conFirmWebhook() {
        try {
            PayOS payOS = payOSProvider.getPayOS();
            String webhookUrl = "";
            ConfirmWebhookResponse result = payOS.webhooks().confirm(webhookUrl);
        } catch (Exception e) {
            log.error("Failed to confirm webhook", e);
        }
    }
}
