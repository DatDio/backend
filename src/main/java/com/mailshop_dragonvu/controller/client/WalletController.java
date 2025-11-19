package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.dto.wallets.WalletResponse;
import com.mailshop_dragonvu.security.UserPrincipal;
import com.mailshop_dragonvu.service.WalletService;
import com.mailshop_dragonvu.utils.Constants;
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

/**
 * Wallet Controller - User wallet and deposit operations
 */
@RestController
@RequestMapping(Constants.API_PATH.WALLETS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quản lý ví", description = "Quản lý số dư người dùng")
@SecurityRequirement(name = "Bearer Authentication")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    @Operation(summary = "Get my wallet", description = "Get current user's wallet information")
    public ApiResponse<WalletResponse> getMyWallet(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        WalletResponse wallet = walletService.getUserWallet(userPrincipal.getId());
        return ApiResponse.success(wallet);
    }

    @PostMapping("payos/deposit")
    @Operation(summary = "Create deposit", description = "Create deposit transaction and get PayOS QR code")
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
    @Operation(summary = "PayOS webhook", description = "PayOS payment callback webhook")
    public ResponseEntity<String> payOSWebhook(@RequestBody Webhook webhook) {

        walletService.processPayOSCallback(webhook);

        return ResponseEntity.ok("OK");
    }

    /**
     * Get my transaction history
     */
    @GetMapping("/transactions")
    @Operation(summary = "Get my transactions", description = "Get current user's transaction history")
    public ResponseEntity<ApiResponse<Page<TransactionResponseDTO>>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionResponseDTO> transactions = walletService.getUserTransactions(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * Get transaction by code
     */
    @GetMapping("/transactions/{transactionCode}")
    @Operation(summary = "Get transaction", description = "Get transaction details by code")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> getTransaction(
            @PathVariable String transactionCode) {

        TransactionResponseDTO transaction = walletService.getTransactionByCode(transactionCode);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }
}
