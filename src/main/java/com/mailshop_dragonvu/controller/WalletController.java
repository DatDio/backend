package com.mailshop_dragonvu.controller;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.request.DepositRequest;
import com.mailshop_dragonvu.dto.response.PayOSPaymentResponse;
import com.mailshop_dragonvu.dto.response.TransactionResponse;
import com.mailshop_dragonvu.dto.response.WalletResponse;
import com.mailshop_dragonvu.security.UserPrincipal;
import com.mailshop_dragonvu.service.WalletService;
import com.mailshop_dragonvu.util.SecurityUtils;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Wallet Controller - User wallet and deposit operations
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallet Management", description = "User wallet operations and deposit management")
@SecurityRequirement(name = "Bearer Authentication")
public class WalletController {

    private final WalletService walletService;

    /**
     * Get current user wallet
     */
    @GetMapping("/me")
    @Operation(summary = "Get my wallet", description = "Get current user's wallet information")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        WalletResponse wallet = walletService.getUserWallet(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    /**
     * Create deposit transaction (generate PayOS QR)
     */
    @PostMapping("/deposit")
    @Operation(summary = "Create deposit", description = "Create deposit transaction and get PayOS QR code")
    public ResponseEntity<ApiResponse<PayOSPaymentResponse>> createDeposit(
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String ipAddress = SecurityUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        PayOSPaymentResponse response = walletService.createDepositTransaction(
                userPrincipal.getId(), request, ipAddress, userAgent);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PayOS webhook callback
     */
    @PostMapping("/payos/webhook")
    @Operation(summary = "PayOS webhook", description = "PayOS payment callback webhook")
    public ResponseEntity<ApiResponse<String>> payOSWebhook(@RequestBody Map<String, Object> payload) {
        log.info("PayOS webhook received: {}", payload);
        
        Long orderCode = Long.valueOf(payload.get("orderCode").toString());
        String status = payload.get("status").toString();
        String transactionReference = payload.getOrDefault("transactionReference", "").toString();
        
        walletService.processPayOSCallback(orderCode, status, transactionReference);
        
        return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully"));
    }

    /**
     * Get my transaction history
     */
    @GetMapping("/transactions")
    @Operation(summary = "Get my transactions", description = "Get current user's transaction history")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TransactionResponse> transactions = walletService.getUserTransactions(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * Get transaction by code
     */
    @GetMapping("/transactions/{transactionCode}")
    @Operation(summary = "Get transaction", description = "Get transaction details by code")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable String transactionCode) {
        
        TransactionResponse transaction = walletService.getTransactionByCode(transactionCode);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Admin: Get user wallet
     */
    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user wallet (Admin)", description = "Admin get any user's wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> getUserWallet(@PathVariable Long userId) {
        WalletResponse wallet = walletService.getUserWallet(userId);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    /**
     * Admin: Adjust user balance
     */
    @PostMapping("/admin/users/{userId}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adjust balance (Admin)", description = "Admin manually adjust user balance")
    public ResponseEntity<ApiResponse<WalletResponse>> adjustBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reason) {
        
        WalletResponse wallet = walletService.adjustBalance(userId, amount, reason);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    /**
     * Admin: Lock wallet
     */
    @PostMapping("/admin/users/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lock wallet (Admin)", description = "Admin lock user wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> lockWallet(
            @PathVariable Long userId,
            @RequestParam String reason) {
        
        WalletResponse wallet = walletService.lockWallet(userId, reason);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    /**
     * Admin: Unlock wallet
     */
    @PostMapping("/admin/users/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock wallet (Admin)", description = "Admin unlock user wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> unlockWallet(@PathVariable Long userId) {
        WalletResponse wallet = walletService.unlockWallet(userId);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }
}
