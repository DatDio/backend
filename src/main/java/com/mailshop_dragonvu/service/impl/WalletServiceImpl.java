package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.request.DepositRequest;
import com.mailshop_dragonvu.dto.response.PayOSPaymentResponse;
import com.mailshop_dragonvu.dto.response.TransactionResponse;
import com.mailshop_dragonvu.dto.response.WalletResponse;
import com.mailshop_dragonvu.entity.Transaction;
import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.entity.Wallet;
import com.mailshop_dragonvu.enums.TransactionStatus;
import com.mailshop_dragonvu.enums.TransactionType;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.TransactionMapper;
import com.mailshop_dragonvu.mapper.WalletMapper;
import com.mailshop_dragonvu.repository.TransactionRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.repository.WalletRepository;
import com.mailshop_dragonvu.service.PayOSService;
import com.mailshop_dragonvu.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Wallet Service Implementation
 * With Anti-DDoS and Anti-Cheat mechanisms
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PayOSService payOSService;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    @Value("${app.payment.min-amount:10000}")
    private BigDecimal minDepositAmount;

    @Value("${app.payment.max-amount:50000000}")
    private BigDecimal maxDepositAmount;

    @Value("${app.security.max-pending-transactions:3}")
    private Integer maxPendingTransactions;

    @Value("${app.security.transaction-timeout-minutes:15}")
    private Integer transactionTimeoutMinutes;

    @Value("${app.security.max-transactions-per-ip-per-hour:10}")
    private Integer maxTransactionsPerIpPerHour;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getUserWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));
        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse createWallet(Long userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.WALLET_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .totalDeposited(BigDecimal.ZERO)
                .totalSpent(BigDecimal.ZERO)
                .isLocked(false)
                .build();

        wallet = walletRepository.save(wallet);
        log.info("Created wallet for user: {}", userId);

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public PayOSPaymentResponse createDepositTransaction(Long userId, DepositRequest request, 
                                                         String ipAddress, String userAgent) {
        log.info("Creating deposit transaction for user: {}, amount: {}, IP: {}", userId, request.getAmount(), ipAddress);

        // Validate user and wallet
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        // Check if wallet is locked
        if (wallet.getIsLocked()) {
            throw new BusinessException(ErrorCode.WALLET_LOCKED);
        }

        // Anti-Cheat: Validate amount
        validateDepositAmount(request.getAmount());

        // Anti-DDoS: Check pending transactions limit
        checkPendingTransactionsLimit(userId);

        // Anti-DDoS: Check IP-based rate limiting
        checkIpRateLimit(ipAddress);

        // Anti-Cheat: Check for duplicate transactions
        checkDuplicateTransactions(userId, request.getAmount());

        // Generate unique order code
        Long orderCode = generateOrderCode();
        String transactionCode = "TXN" + orderCode;

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .transactionCode(transactionCode)
                .user(user)
                .wallet(wallet)
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceBefore(wallet.getBalance())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription() != null ? request.getDescription() : "Nạp tiền vào ví")
                .paymentMethod("PAYOS")
                .payosOrderCode(orderCode)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction created: {}", transactionCode);

        // Create PayOS payment link
        String returnUrl = request.getReturnUrl() != null ? request.getReturnUrl() : frontendUrl + "/payment/success";
        String cancelUrl = request.getCancelUrl() != null ? request.getCancelUrl() : frontendUrl + "/payment/cancel";

        PayOSPaymentResponse paymentResponse = payOSService.createPaymentLink(
                orderCode,
                request.getAmount(),
                "Nạp tiền - " + user.getEmail(),
                returnUrl,
                cancelUrl
        );

        paymentResponse.setTransactionCode(transactionCode);

        return paymentResponse;
    }

    @Override
    @Transactional
    public void processPayOSCallback(Long orderCode, String status, String transactionReference) {
        log.info("Processing PayOS callback - OrderCode: {}, Status: {}", orderCode, status);

        Transaction transaction = transactionRepository.findByPayosOrderCode(orderCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Check if already processed
        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            log.warn("Transaction already processed: {}", transaction.getTransactionCode());
            return;
        }

        // Check transaction timeout (15 minutes)
        if (transaction.getCreatedAt().plusMinutes(transactionTimeoutMinutes).isBefore(LocalDateTime.now())) {
            transaction.markAsFailed("Transaction timeout");
            transactionRepository.save(transaction);
            log.warn("Transaction timeout: {}", transaction.getTransactionCode());
            return;
        }

        if ("PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
            // Use pessimistic lock to prevent race conditions
            Wallet wallet = walletRepository.findByUserIdWithLock(transaction.getUser().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

            // Add balance
            wallet.addBalance(transaction.getAmount());
            transaction.setBalanceAfter(wallet.getBalance());
            transaction.setPaymentReference(transactionReference);
            transaction.markAsSuccess();

            walletRepository.save(wallet);
            transactionRepository.save(transaction);

            log.info("Deposit successful - User: {}, Amount: {}, New Balance: {}", 
                    transaction.getUser().getId(), transaction.getAmount(), wallet.getBalance());

        } else {
            transaction.markAsFailed("Payment failed or cancelled");
            transactionRepository.save(transaction);
            log.warn("Payment failed for transaction: {}", transaction.getTransactionCode());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByCode(String transactionCode) {
        Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public WalletResponse adjustBalance(Long userId, BigDecimal amount, String reason) {
        log.info("Admin adjusting balance for user: {}, amount: {}, reason: {}", userId, amount, reason);

        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        User user = wallet.getUser();

        // Create admin adjustment transaction
        Transaction transaction = Transaction.builder()
                .transactionCode("ADJ" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .wallet(wallet)
                .type(TransactionType.ADMIN_ADJUST)
                .amount(amount.abs())
                .balanceBefore(wallet.getBalance())
                .status(TransactionStatus.SUCCESS)
                .description(reason != null ? reason : "Admin adjustment")
                .paymentMethod("ADMIN")
                .build();

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            wallet.addBalance(amount);
        } else {
            wallet.deductBalance(amount.abs());
        }

        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setCompletedAt(LocalDateTime.now());

        walletRepository.save(wallet);
        transactionRepository.save(transaction);

        log.info("Balance adjusted - User: {}, New Balance: {}", userId, wallet.getBalance());

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse lockWallet(Long userId, String reason) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setIsLocked(true);
        wallet.setLockReason(reason);
        wallet = walletRepository.save(wallet);

        log.warn("Wallet locked for user: {}, reason: {}", userId, reason);

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse unlockWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setIsLocked(false);
        wallet.setLockReason(null);
        wallet = walletRepository.save(wallet);

        log.info("Wallet unlocked for user: {}", userId);

        return walletMapper.toResponse(wallet);
    }

    /**
     * ANTI-CHEAT: Validate deposit amount
     */
    private void validateDepositAmount(BigDecimal amount) {
        if (amount.compareTo(minDepositAmount) < 0) {
            throw new BusinessException(ErrorCode.DEPOSIT_AMOUNT_TOO_LOW);
        }
        if (amount.compareTo(maxDepositAmount) > 0) {
            throw new BusinessException(ErrorCode.DEPOSIT_AMOUNT_TOO_HIGH);
        }
        // Check if amount is a valid number (no decimals for VND)
        if (amount.scale() > 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT_FORMAT);
        }
    }

    /**
     * ANTI-DDOS: Check pending transactions limit per user
     */
    private void checkPendingTransactionsLimit(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(transactionTimeoutMinutes);
        Long pendingCount = transactionRepository.countPendingTransactionsSince(userId, since);

        if (pendingCount >= maxPendingTransactions) {
            throw new BusinessException(ErrorCode.TOO_MANY_PENDING_TRANSACTIONS);
        }
    }

    /**
     * ANTI-DDOS: Check IP-based rate limiting
     */
    private void checkIpRateLimit(String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        Long transactionCount = transactionRepository.countTransactionsByIpSince(ipAddress, since);

        if (transactionCount >= maxTransactionsPerIpPerHour) {
            log.warn("IP rate limit exceeded: {}, count: {}", ipAddress, transactionCount);
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    /**
     * ANTI-CHEAT: Check for duplicate transactions
     */
    private void checkDuplicateTransactions(Long userId, BigDecimal amount) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<Transaction> duplicates = transactionRepository.findDuplicateTransactions(userId, amount, since);

        if (!duplicates.isEmpty()) {
            log.warn("Duplicate transaction attempt - User: {}, Amount: {}", userId, amount);
            throw new BusinessException(ErrorCode.DUPLICATE_TRANSACTION);
        }
    }

    /**
     * Generate unique order code for PayOS
     */
    private Long generateOrderCode() {
        return System.currentTimeMillis();
    }
}
