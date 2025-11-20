package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.dto.wallets.WalletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;

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
    void createWallet(Long userId);

    /**
     * Create deposit transaction and generate PayOS QR
     */
    CreatePaymentLinkResponse createDepositPayOS(Long userId, CreatePaymentLinkRequest request, String ipAddress, String userAgent);

    /**
     * Process PayOS webhook callback
     */
    void processPayOSCallback(Webhook webhook);

    /**
     * Get user transaction history
     */
    Page<TransactionResponseDTO> getUserTransactions(Long userId, Pageable pageable);

    /**
     * Get transaction by code
     */
    TransactionResponseDTO getTransactionByCode(Long transactionCode);

    /**
     * Admin: Adjust user balance
     */
    WalletResponse adjustBalance(Long userId, Long amount, String reason);

    /**
     * Admin: Lock/Unlock wallet
     */
    WalletResponse lockWallet(Long userId, String reason);

    WalletResponse unlockWallet(Long userId);
}
