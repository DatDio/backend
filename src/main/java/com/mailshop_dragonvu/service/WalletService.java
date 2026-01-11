package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.casso.CassoDepositResponse;
import com.mailshop_dragonvu.dto.casso.CassoWebhookDTO;
import com.mailshop_dragonvu.dto.fpayment.FPaymentDepositResponse;
import com.mailshop_dragonvu.dto.fpayment.FPaymentWebhookDTO;
import com.mailshop_dragonvu.dto.transactions.TransactionFilterDTO;
import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.dto.users.UserFilterDTO;
import com.mailshop_dragonvu.dto.users.UserResponseDTO;
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
     * Create deposit transaction and generate Casso/VietQR
     */
    CassoDepositResponse createDepositCasso(Long userId, Long amount, String ipAddress, String userAgent);

    /**
     * Process Casso webhook callback
     */
    void processCassoCallback(CassoWebhookDTO webhook);

    /**
     * Create deposit transaction via FPayment/Crypto
     * @param userId User ID
     * @param amountUsdt Amount in USDT (directly from frontend)
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     * @return FPayment deposit response with payment URL
     */
    FPaymentDepositResponse createDepositFPayment(Long userId, java.math.BigDecimal amountUsdt, String ipAddress, String userAgent);

    /**
     * Process FPayment webhook callback
     * @param webhook Webhook data from FPayment
     */
    void processFPaymentCallback(FPaymentWebhookDTO webhook);

    /**
     * Check FPayment invoice status and process if completed
     * Used for polling when callback doesn't work
     * @param transactionCode Internal transaction code
     * @return Status of the transaction (waiting, completed, expired, not_found, already_processed)
     */
    String checkFPaymentStatus(Long transactionCode);

    /**
     * Get user transaction history
     */
    Page<TransactionResponseDTO> getUserTransactions(Long userId, Pageable pageable);

    Page<TransactionResponseDTO> searchUserTransactions(Long userId, TransactionFilterDTO transactionFilterDTO);
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

    void deleteTransaction(Long transactionId);

    void deleteByTransactionCode(Long orderCode);
    /**
     * Deduct balance when user purchases items
     */
    WalletResponse spend(Long userId, Long amount, String description);

    /**
     * Admin: Search all transactions with filter
     */
    Page<TransactionResponseDTO> searchAllTransactions(TransactionFilterDTO filter);

    /**
     * Admin: Update transaction status (PENDING -> SUCCESS/FAILED)
     * If changing to SUCCESS for DEPOSIT type, auto add balance to user wallet
     */
    TransactionResponseDTO updateTransactionStatus(Long transactionId, Integer newStatus, String reason);
}

