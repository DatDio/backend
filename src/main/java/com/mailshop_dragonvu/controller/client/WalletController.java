package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.casso.CassoDepositResponse;
import com.mailshop_dragonvu.dto.casso.CassoWebhookDTO;
import com.mailshop_dragonvu.dto.fpayment.FPaymentDepositResponse;
import com.mailshop_dragonvu.dto.fpayment.FPaymentWebhookDTO;
import com.mailshop_dragonvu.dto.transactions.TransactionFilterDTO;
import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.dto.wallets.WalletResponse;
import com.mailshop_dragonvu.security.UserPrincipal;
import com.mailshop_dragonvu.service.CassoService;
import com.mailshop_dragonvu.service.MessageService;
import com.mailshop_dragonvu.service.WalletService;
import com.mailshop_dragonvu.utils.Constants;
import com.mailshop_dragonvu.utils.MessageKeys;
import com.mailshop_dragonvu.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(Constants.API_PATH.WALLETS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quản lý ví", description = "Quản lý số dư người dùng")
@SecurityRequirement(name = "Bearer Authentication")
public class WalletController {

    private final WalletService walletService;
    private final CassoService cassoService;
    private final MessageService messageService;

    @GetMapping("/me")
    public ApiResponse<WalletResponse> getMyWallet(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        WalletResponse wallet = walletService.getUserWallet(userPrincipal.getId());
        return ApiResponse.success(wallet);
    }

    @PostMapping("/payos/deposit")
    public ApiResponse<CreatePaymentLinkResponse> createDeposit(
            @Valid @RequestBody CreatePaymentLinkRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {

        String ipAddress = SecurityUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        CreatePaymentLinkResponse response = walletService.createDepositPayOS(
                userPrincipal.getId(), request, ipAddress, userAgent);

        return ApiResponse.success(response);
    }

    /**
     * PayOS webhook callback
     */
    @PostMapping("/payos/webhook")
    public ResponseEntity<String> payOSWebhook(@RequestBody Webhook webhook) {

        walletService.processPayOSCallback(webhook);

        return ResponseEntity.ok("OK");
    }

    // ==================== CASSO ENDPOINTS ====================

    /**
     * Create deposit with Casso/VietQR
     */
    @PostMapping("/casso/deposit")
    @Operation(summary = "Tạo yêu cầu nạp tiền qua VietQR/Casso")
    public ApiResponse<CassoDepositResponse> createDepositCasso(
            @RequestParam Long amount,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {

        String ipAddress = SecurityUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        CassoDepositResponse response = walletService.createDepositCasso(
                userPrincipal.getId(), amount, ipAddress, userAgent);

        return ApiResponse.success(response);
    }

    /**
     * Casso webhook callback - receives bank transfer notifications
     */
    @PostMapping("/casso/webhook")
    @Operation(summary = "Webhook nhận thông báo từ Casso")
    public ResponseEntity<String> cassoWebhook(
            @RequestHeader(value = "secure-token", required = false) String secureToken,
            @RequestBody(required = false) CassoWebhookDTO webhook) {

        log.info("Received Casso webhook: {}", webhook);

        // Handle empty body (for testing connectivity)
        if (webhook == null || webhook.getData() == null || webhook.getData().isEmpty()
                || webhook.getData().get(0).getId() == 0) {
            log.info("Casso webhook test/verification request received");
            return ResponseEntity.ok("OK");
        }

        // Verify webhook authenticity
        if (!cassoService.verifyWebhook(secureToken)) {
            log.warn("Invalid Casso webhook secure token");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // Process the webhook
        walletService.processCassoCallback(webhook);

        // Return 200 OK as required by Casso
        return ResponseEntity.ok("OK");
    }

    /**
     * Casso webhook verification endpoint (GET request for testing)
     */
    @GetMapping("/casso/webhook")
    @Operation(summary = "Verify Casso webhook endpoint is accessible")
    public ResponseEntity<String> cassoWebhookVerify() {
        log.info("Casso webhook GET verification request received");
        return ResponseEntity.ok("OK");
    }

    // ==================== END CASSO ENDPOINTS ====================

    // ==================== FPAYMENT ENDPOINTS ====================

    /**
     * Create deposit with FPayment/Crypto (USDT)
     */
    @PostMapping("/fpayment/deposit")
    @Operation(summary = "Tạo yêu cầu nạp tiền qua FPayment/Crypto (USDT)")
    public ApiResponse<FPaymentDepositResponse> createDepositFPayment(
            @RequestParam Long amountVnd,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {

        String ipAddress = SecurityUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        FPaymentDepositResponse response = walletService.createDepositFPayment(
                userPrincipal.getId(), amountVnd, ipAddress, userAgent);

        return ApiResponse.success(response);
    }

    /**
     * FPayment webhook callback - receives payment notifications (GET method as per FPayment docs)
     */
    @GetMapping("/fpayment/webhook")
    @Operation(summary = "Webhook nhận thông báo từ FPayment")
    public ResponseEntity<String> fpaymentWebhook(
            @RequestParam(value = "request_id", required = false) String requestId,
            @RequestParam(value = "trans_id", required = false) String transId,
            @RequestParam(value = "merchant_id", required = false) String merchantId,
            @RequestParam(value = "api_key", required = false) String apiKey,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "received", required = false) String received,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "from_address", required = false) String fromAddress,
            @RequestParam(value = "transaction_id", required = false) String transactionId) {

        log.info("Received FPayment webhook - requestId: {}, transId: {}, status: {}", 
                requestId, transId, status);

        // Handle empty/test requests
        if (requestId == null || status == null) {
            log.info("FPayment webhook test/verification request received");
            return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Callback đã được xử lý thành công.\"}");
        }

        // Build webhook DTO
        FPaymentWebhookDTO webhook = FPaymentWebhookDTO.builder()
                .requestId(requestId)
                .transId(transId)
                .merchantId(merchantId)
                .apiKey(apiKey)
                .amount(amount)
                .received(received)
                .status(status)
                .fromAddress(fromAddress)
                .transactionId(transactionId)
                .build();

        // Process the webhook
        walletService.processFPaymentCallback(webhook);

        // Return success response as required by FPayment
        return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Callback đã được xử lý thành công.\"}");
    }

    // ==================== END FPAYMENT ENDPOINTS ====================

    @GetMapping("/transactions/search")
    @Operation(summary = "Search my transactions with filter")
    public ApiResponse<Page<TransactionResponseDTO>> searchTransactions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            TransactionFilterDTO filterDTO
    ) {
        return ApiResponse.success(
                walletService.searchUserTransactions(userPrincipal.getId(), filterDTO)
        );
    }


    /**
     * Get transaction by code
     */
    @GetMapping("/transactions/{transactionCode}")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> getTransaction(
            @PathVariable Long transactionCode) {

        TransactionResponseDTO transaction = walletService.getTransactionByCode(transactionCode);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @DeleteMapping("/transactions/delete/{orderCode}")
    public ApiResponse<Void> deleteTransaction(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable Long orderCode) {
        walletService.deleteByTransactionCode(orderCode);
        return ApiResponse.success(messageService.getMessage(MessageKeys.Transaction.DELETED));
    }
}

