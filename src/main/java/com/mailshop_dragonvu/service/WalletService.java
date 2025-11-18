package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.payos.DepositRequest;
import com.mailshop_dragonvu.dto.payos.PayOSPaymentResponse;
import com.mailshop_dragonvu.dto.transactions.TransactionResponse;
import com.mailshop_dragonvu.dto.wallets.WalletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Wallet Service Interface
 */
public interface WalletService {

    /**
     * Get user wallet
     */
    WalletResponse getUserWallet(Long userId);

    /**
     * Create wallet for new user
     */
    WalletResponse createWallet(Long userId);

    /**
     * Create deposit transaction and generate PayOS QR
     */
    PayOSPaymentResponse createDepositTransaction(Long userId, DepositRequest request, String ipAddress, String userAgent);

    /**
     * Process PayOS webhook callback
     */
    void processPayOSCallback(Long orderCode, String status, String transactionReference);

    /**
     * Get user transaction history
     */
    Page<TransactionResponse> getUserTransactions(Long userId, Pageable pageable);

    /**
     * Get transaction by code
     */
    TransactionResponse getTransactionByCode(String transactionCode);

    /**
     * Admin: Adjust user balance
     */
    WalletResponse adjustBalance(Long userId, BigDecimal amount, String reason);

    /**
     * Admin: Lock/Unlock wallet
     */
    WalletResponse lockWallet(Long userId, String reason);
    WalletResponse unlockWallet(Long userId);
}
