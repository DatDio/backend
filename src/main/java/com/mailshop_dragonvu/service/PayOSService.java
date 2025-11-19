package com.mailshop_dragonvu.service;

import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

/**
 * PayOS Service Interface - PayOS Payment Gateway Integration
 */
public interface PayOSService {

    //Tạo link QR thanh toán PayOS
    CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest createPaymentLinkRequest);

    //Webhook nhận từ PayOS
    WebhookData verifyWebhook(Webhook webhook);

    //Xác nhận setu thành công webhookUrl đến PayOS
    void conFirmWebhook();
}
