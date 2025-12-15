package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.transactions.TransactionFilterDTO;
import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.dto.wallets.WalletResponse;
import com.mailshop_dragonvu.entity.TransactionEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.entity.WalletEntity;
import com.mailshop_dragonvu.enums.TransactionStatusEnum;
import com.mailshop_dragonvu.enums.TransactionTypeEnum;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.TransactionMapper;
import com.mailshop_dragonvu.mapper.WalletMapper;
import com.mailshop_dragonvu.repository.TransactionRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.repository.WalletRepository;
import com.mailshop_dragonvu.service.PayOSService;
import com.mailshop_dragonvu.service.RankService;
import com.mailshop_dragonvu.service.WalletService;
import com.mailshop_dragonvu.utils.Utils;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PayOSService payOSService;
    private final RankService rankService;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    @Value("${app.payment.min-amount:10000}")
    private Long minDepositAmount;

    @Value("${app.payment.max-amount:50000000}")
    private Long maxDepositAmount;

    @Value("${app.security.max-pending-transactions:3}")
    private Integer maxPendingTransactions;

    @Value("${app.security.transaction-timeout-minutes:15}")
    private Integer transactionTimeoutMinutes;

    @Value("${app.security.max-transactions-per-ip-per-hour:10}")
    private Integer maxTransactionsPerIpPerHour;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${payos.checksum-key}")
    private String secretKey;

    @Override
    @Transactional
    public WalletResponse getUserWallet(Long userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        WalletEntity walletEntity = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    WalletEntity newWallet = WalletEntity.builder()
                            .user(userEntity)
                            .balance(0L)
                            .totalDeposited(0L)
                            .totalSpent(0L)
                            .isLocked(false)
                            .build();
                    return walletRepository.save(newWallet);
                });

        return walletMapper.toResponse(walletEntity);
    }

    @Override
    @Transactional
    public void createWallet(Long userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.WALLET_ALREADY_EXISTS);
        }

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        WalletEntity walletEntity = WalletEntity.builder()
                .user(userEntity)
                .balance(0L)
                .totalDeposited(0L)
                .totalSpent(0L)
                .isLocked(false)
                .build();

        walletRepository.save(walletEntity);
    }

    @Override
    @Transactional
    public CreatePaymentLinkResponse createDepositPayOS(Long userId, CreatePaymentLinkRequest request,
            String ipAddress, String userAgent) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        WalletEntity walletEntity = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    createWallet(userId);
                    return walletRepository.findByUserId(userId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));
                });

        if (walletEntity.getIsLocked()) {
            throw new BusinessException(ErrorCode.WALLET_LOCKED);
        }

        // Validate
        validateDepositAmount(request.getAmount());
        // checkIpRateLimit(ipAddress);
        // checkPendingTransactionsLimit(userId);
        // checkDuplicateTransactions(userId, request.getAmount());

        // Tạo PayOS payment link
        CreatePaymentLinkResponse payOS = payOSService.createPaymentLink(request);

        // Tạo giao dịch PENDING
        TransactionEntity txn = TransactionEntity.builder()
                .transactionCode(payOS.getOrderCode())
                .user(userEntity)
                .wallet(walletEntity)
                .type(TransactionTypeEnum.DEPOSIT)
                .amount(request.getAmount())
                .balanceBefore(walletEntity.getBalance())
                .status(TransactionStatusEnum.PENDING)
                .description(request.getDescription() != null ? request.getDescription() : "Nạp tiền PayOS")
                .paymentMethod("PAYOS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .completedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(txn);

        return payOS;
    }

    @Override
    @Transactional
    public void processPayOSCallback(Webhook webhook) {
        //WebhookData data = payOSService.verifyWebhook(webhook);

        WebhookData data = new WebhookData();
        data.setOrderCode(webhook.getData().getOrderCode());
        data.setCode("00");
        data.setAmount(webhook.getData().getAmount());

        Long orderCode = data.getOrderCode();

        // Lấy transaction theo orderCode
        TransactionEntity txn = transactionRepository.findByTransactionCode(orderCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Nếu đã xử lý rồi → bỏ qua để tránh double webhook
        if (txn.getStatus() == TransactionStatusEnum.SUCCESS) {
            return;
        }

        // Nếu thất bại → xóa luôn transaction
        if (!"00".equalsIgnoreCase(data.getCode())) {

            transactionRepository.delete(txn);
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK);
        }

        // Thành công → cộng tiền + bonus
        try {
            WalletEntity walletEntity = walletRepository.findByUserIdWithLock(txn.getUser().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

            Long depositAmount = data.getAmount();
            Long userId = txn.getUser().getId();
            
            // Calculate bonus based on user's rank
            Long bonusAmount = rankService.calculateDepositBonus(userId, depositAmount);
            Long totalAmount = depositAmount + bonusAmount;
            
            // Add both deposit and bonus to wallet
            walletEntity.addBalance(totalAmount);
            
            // Update transaction with bonus info
            if (bonusAmount > 0) {
                String originalDesc = txn.getDescription() != null ? txn.getDescription() : "Nạp tiền PayOS";
                txn.setDescription(originalDesc + " (Bonus +" + bonusAmount + " VNĐ)");
            }
            
            txn.setBalanceAfter(walletEntity.getBalance());
            txn.setPaymentReference(data.getReference());
            txn.markAsSuccess();

            walletRepository.save(walletEntity);
            transactionRepository.save(txn);
            
            log.info("Deposit successful - User: {}, Amount: {}, Bonus: {}, Total: {}", 
                    userId, depositAmount, bonusAmount, totalAmount);

        } catch (PessimisticLockException | CannotAcquireLockException e) {

            log.warn("Wallet đang lock. Bỏ qua webhook lần này.");
        }

    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> searchUserTransactions(Long userId, TransactionFilterDTO filterDTO) {

        Sort sort = Utils.generatedSort(filterDTO.getSort());
        Pageable pageable = PageRequest.of(
                filterDTO.getPage(),
                filterDTO.getLimit(),
                sort);

        Specification<TransactionEntity> specification = buildTransactionSpecification(userId, filterDTO);

        return transactionRepository
                .findAll(specification, pageable)
                .map(transactionMapper::toResponse);
    }

    private Specification<TransactionEntity> buildTransactionSpecification(
            Long userId,
            TransactionFilterDTO request) {
        return (Root<TransactionEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ✅ Giao dịch thuộc user hiện tại
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // ✅ Filter theo mã giao dịch (Long -> String for like search)
            if (Strings.isNotBlank(request.getTransactionCode())) {
                predicates.add(
                        cb.like(
                                root.get("transactionCode").as(String.class),
                                "%" + request.getTransactionCode().trim() + "%"));
            }

            // ✅ From date
            if (request.getDateFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                request.getDateFrom()));
            }

            // ✅ To date
            if (request.getDateTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("createdAt"),
                                request.getDateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionByCode(Long transactionCode) {
        TransactionEntity transactionEntity = transactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));
        return transactionMapper.toResponse(transactionEntity);
    }

    @Override
    @Transactional
    public WalletResponse spend(Long userId, Long amount, String description) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT);
        }

        WalletEntity walletEntity = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        if (walletEntity.getIsLocked()) {
            throw new BusinessException(ErrorCode.WALLET_LOCKED);
        }
        if (!walletEntity.hasSufficientBalance(amount)) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        UserEntity userEntity = walletEntity.getUser();
        Long balanceBefore = walletEntity.getBalance();

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .transactionCode(generateOrderCode())
                .user(userEntity)
                .wallet(walletEntity)
                .type(TransactionTypeEnum.PURCHASE)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .status(TransactionStatusEnum.SUCCESS)
                .description(description != null ? description : "Thanh toán đơn hàng")
                .paymentMethod("WALLET")
                .build();

        walletEntity.deductBalance(amount);
        transactionEntity.setBalanceAfter(walletEntity.getBalance());
        transactionEntity.setCompletedAt(LocalDateTime.now());

        walletRepository.save(walletEntity);
        transactionRepository.save(transactionEntity);

        return walletMapper.toResponse(walletEntity);
    }

    @Override
    @Transactional
    public WalletResponse adjustBalance(Long userId, Long amount, String reason) {

        if (amount == null || amount == 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT);
        }

        // Lấy wallet + khóa hàng để tránh race condition
        WalletEntity walletEntity = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        UserEntity userEntity = walletEntity.getUser();

        Long balanceBefore = walletEntity.getBalance();

        // Xác định loại điều chỉnh: cộng hay trừ
        boolean isIncrease = amount > 0;

        // Tạo transaction
        TransactionEntity transactionEntity = TransactionEntity.builder()
                .transactionCode(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000))
                .user(userEntity)
                .wallet(walletEntity)
                .type(TransactionTypeEnum.ADMIN_ADJUST)
                .amount(Math.abs(amount)) // transaction nên lưu số dương
                .balanceBefore(balanceBefore)
                .status(TransactionStatusEnum.SUCCESS)
                .description(reason != null ? reason : (isIncrease ? "Admin tăng số dư" : "Admin giảm số dư"))
                .paymentMethod("ADMIN")
                .build();

        // Update balance
        if (isIncrease) {
            walletEntity.addBalance(amount);
        } else {
            // amount là số âm -> trừ theo số dương
            walletEntity.deductBalance(Math.abs(amount));
        }

        transactionEntity.setBalanceAfter(walletEntity.getBalance());
        transactionEntity.setCompletedAt(LocalDateTime.now());

        walletRepository.save(walletEntity);
        transactionRepository.save(transactionEntity);

        return walletMapper.toResponse(walletEntity);
    }

    @Override
    @Transactional
    public WalletResponse lockWallet(Long userId, String reason) {
        WalletEntity walletEntity = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        walletEntity.setIsLocked(true);
        walletEntity.setLockReason(reason);
        walletEntity = walletRepository.save(walletEntity);

        log.warn("Wallet locked for user: {}, reason: {}", userId, reason);

        return walletMapper.toResponse(walletEntity);
    }

    @Override
    @Transactional
    public WalletResponse unlockWallet(Long userId) {
        WalletEntity walletEntity = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        walletEntity.setIsLocked(false);
        walletEntity.setLockReason(null);
        walletEntity = walletRepository.save(walletEntity);

        log.info("Wallet unlocked for user: {}", userId);

        return walletMapper.toResponse(walletEntity);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId) {
        transactionRepository.deleteById(transactionId);
    }

    @Override
    @Transactional
    public void deleteByTransactionCode(Long orderCode) {
        transactionRepository.deleteByTransactionCode(orderCode);
    }

    /**
     * ANTI-CHEAT: Validate deposit amount
     */
    private void validateDepositAmount(Long amount) {
        if (amount.compareTo(minDepositAmount) < 0) {
            throw new BusinessException(ErrorCode.DEPOSIT_AMOUNT_TOO_LOW);
        }
        // if (amount.compareTo(maxDepositAmount) > 0) {
        // throw new BusinessException(ErrorCode.DEPOSIT_AMOUNT_TOO_HIGH);
        // }
        // Check if amount is a valid number (no decimals for VND)
        // if (amount.scale() > 0) {
        // throw new BusinessException(ErrorCode.INVALID_AMOUNT_FORMAT);
        // }
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
    private void checkDuplicateTransactions(Long userId, Long amount) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<TransactionEntity> duplicates = transactionRepository.findDuplicateTransactions(userId, amount, since);

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
