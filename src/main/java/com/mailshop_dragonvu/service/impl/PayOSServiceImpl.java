package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.service.PayOSService;
import com.mailshop_dragonvu.utils.DepositCodeUtil;
import com.mailshop_dragonvu.utils.SecurityUtils;
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
    @Value("${payos.checksum-key}")
    private String secretKey;

    private final PayOS payOS;

    @Override
    public CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest createPaymentLinkRequest) {

        try {
            //final String productName = createPaymentLinkRequest.getProductName();
            final String description = createPaymentLinkRequest.getDescription();

            final String returnUrl = createPaymentLinkRequest.getReturnUrl();
            final String cancelUrl = createPaymentLinkRequest.getCancelUrl();
            long orderCode = System.currentTimeMillis() / 1000;
            // PaymentLinkItem item =PaymentLinkItem.builder().name(productName).quantity(1).price(price).build();

            CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(System.currentTimeMillis() / 1000)
                    .amount(2000L)
                    .description("NAPTIEN")
                    .cancelUrl("https://your-domain.com/cancel")
                    .returnUrl("https://your-domain.com/success")
                    .build();
            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentRequest);
            return data;
        } catch (Exception e) {
            log.error("PayOS create link failed", e);
            throw new BusinessException(ErrorCode.PAYMENT_CREATION_FAILED);
        }
    }

    @Override
    public WebhookData verifyWebhook(Webhook webhook) {
        try {
            WebhookData data = payOS.webhooks().verify(webhook);
            if (!"00".equals(data.getCode())) {
                throw new BusinessException(ErrorCode.INVALID_WEBHOOK);
            }
            return data;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK);
        }
    }

    @Override
    public void conFirmWebhook() {
        try{
            String webhookUrl = "";
            ConfirmWebhookResponse result = payOS.webhooks().confirm(webhookUrl);
        }
        catch (Exception e){

        }

    }


}
