package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.casso.CassoDepositResponse;
import com.mailshop_dragonvu.dto.casso.CassoTransactionDTO;
import com.mailshop_dragonvu.dto.casso.CassoWebhookDTO;
import com.mailshop_dragonvu.dto.fpayment.FPaymentApiResponse;
import com.mailshop_dragonvu.dto.fpayment.FPaymentDepositResponse;
import com.mailshop_dragonvu.dto.fpayment.FPaymentWebhookDTO;
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
import com.mailshop_dragonvu.service.CassoProvider;
import com.mailshop_dragonvu.service.CassoService;
import com.mailshop_dragonvu.service.DepositNotifier;
import com.mailshop_dragonvu.service.FPaymentProvider;
import com.mailshop_dragonvu.service.FPaymentService;
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
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PayOSService payOSService;
    private final CassoService cassoService;
    private final CassoProvider cassoProvider;
    private final FPaymentService fpaymentService;
    private final FPaymentProvider fpaymentProvider;
    private final RankService rankService;
    private final DepositNotifier depositNotifier;
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
        WebhookData data = payOSService.verifyWebhook(webhook);

//        WebhookData data = new WebhookData();
//        data.setOrderCode(webhook.getData().getOrderCode());
//        data.setCode("00");
//        data.setAmount(webhook.getData().getAmount());

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

    // ==================== CASSO/VIETQR METHODS ====================

    @Override
    @Transactional
    public CassoDepositResponse createDepositCasso(Long userId, Long amount, String ipAddress, String userAgent) {
        
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

        // Validate amount
        validateDepositAmount(amount);

        // Generate unique transaction code (timestamp + 3 random digits to prevent collision)
        Long transactionCode = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(100, 999);
        String transferContent = String.valueOf(transactionCode);

        // Generate VietQR URL
        String qrCodeUrl = cassoService.generateQRCodeUrl(amount, transactionCode);

        // Create PENDING transaction
        TransactionEntity txn = TransactionEntity.builder()
                .transactionCode(transactionCode)
                .user(userEntity)
                .wallet(walletEntity)
                .type(TransactionTypeEnum.DEPOSIT)
                .amount(amount)
                .balanceBefore(walletEntity.getBalance())
                .status(TransactionStatusEnum.PENDING)
                .description(transferContent)
                .paymentMethod("CASSO")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(txn);

        log.info("Created Casso deposit - User: {}, Amount: {}, TransactionCode: {}", 
                userId, amount, transactionCode);

        return CassoDepositResponse.builder()
                .qrCodeUrl(qrCodeUrl)
                .transactionCode(transactionCode)
                .amount(amount)
                .transferContent(transferContent)
                .bankName(cassoService.getBankName(cassoProvider.getBankCode()))
                .accountNumber(cassoProvider.getBankAccount())
                .accountName(cassoProvider.getAccountName())
                .build();
    }

    @Override
    @Transactional
    public void processCassoCallback(CassoWebhookDTO webhook) {
        if (webhook == null || webhook.getData() == null || webhook.getData().isEmpty()) {
            log.warn("Empty Casso webhook received");
            return;
        }

        // Check for error
        if (webhook.getError() != null && webhook.getError() != 0) {
            log.warn("Casso webhook returned error: {}", webhook.getError());
            return;
        }

        // Process each transaction in the list
        for (CassoTransactionDTO txnData : webhook.getData()) {
            try {
                processCassoTransaction(txnData);
            } catch (Exception e) {
                log.error("Failed to process Casso transaction: {}", txnData.getId(), e);
            }
        }
    }

    private void processCassoTransaction(CassoTransactionDTO txnData) {
        // Pattern to match transaction code (just numbers)
        Pattern pattern = Pattern.compile("(\\d{13,})");
        
        String description = txnData.getDescription();
        if (Strings.isBlank(description)) {
            log.debug("Skipping Casso transaction without description: {}", txnData.getId());
            return;
        }

        // Try to extract transaction code from description (remove spaces)
        Matcher matcher = pattern.matcher(description.replaceAll("\\s+", ""));
        if (!matcher.find()) {
            log.debug("No transaction code found in description: {}", description);
            return;
        }

        String codeStr = matcher.group(1);
        Long transactionCode;
        try {
            transactionCode = Long.parseLong(codeStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid transaction code in description: {}", codeStr);
            return;
        }

        // Check for duplicate using Casso ID
        if (txnData.getId() != null) {
            boolean exists = transactionRepository.existsByPaymentReference(String.valueOf(txnData.getId()));
            if (exists) {
                log.debug("Casso transaction already processed: {}", txnData.getId());
                return;
            }
        }

        // Find matching PENDING transaction
        Optional<TransactionEntity> optTxn = transactionRepository.findByTransactionCode(transactionCode);
        if (optTxn.isEmpty()) {
            log.debug("No matching transaction found for code: {}", transactionCode);
            return;
        }

        TransactionEntity txn = optTxn.get();

        // Skip if already processed
        if (txn.getStatus() == TransactionStatusEnum.SUCCESS) {
            log.debug("Transaction already processed: {}", transactionCode);
            return;
        }

        // Verify amount matches (allow some tolerance for fees)
        Long webhookAmount = txnData.getAmount();
        if (webhookAmount == null) {
            log.warn("Casso webhook missing amount for transaction: {}", transactionCode);
            return;
        }
        
        if (!txn.getAmount().equals(webhookAmount)) {
            log.warn("Amount mismatch - Expected: {}, Got: {} for transaction: {}", 
                    txn.getAmount(), webhookAmount, transactionCode);
            // Still process if amount is greater (in case of tips)
            if (webhookAmount < txn.getAmount()) {
                return;
            }
        }

        // Process successful deposit
        try {
            WalletEntity walletEntity = walletRepository.findByUserIdWithLock(txn.getUser().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

            Long depositAmount = webhookAmount;
            Long userId = txn.getUser().getId();

            // Calculate bonus based on user's rank
            Long bonusAmount = rankService.calculateDepositBonus(userId, depositAmount);
            Long totalAmount = depositAmount + bonusAmount;

            // Add to wallet
            walletEntity.addBalance(totalAmount);

            // Update transaction
            if (bonusAmount > 0) {
                txn.setDescription(txn.getDescription() + " (Bonus +" + bonusAmount + " VNĐ)");
            }
            txn.setAmount(depositAmount); // Update to actual received amount
            txn.setBalanceAfter(walletEntity.getBalance());
            txn.setPaymentReference(txnData.getId() != null ? String.valueOf(txnData.getId()) : txnData.getReference());
            txn.markAsSuccess();

            walletRepository.save(walletEntity);
            transactionRepository.save(txn);

            log.info("Casso deposit successful - User: {}, Amount: {}, Bonus: {}, Total: {}", 
                    userId, depositAmount, bonusAmount, totalAmount);

            // Send WebSocket notification to frontend
            depositNotifier.notifyDepositSuccess(
                    userId, 
                    transactionCode, 
                    depositAmount, 
                    bonusAmount, 
                    totalAmount, 
                    walletEntity.getBalance()
            );

        } catch (PessimisticLockException | CannotAcquireLockException e) {
            log.warn("Wallet locked during Casso callback. Transaction: {}", transactionCode);
        }
    }

    // ==================== END CASSO METHODS ====================

    // ==================== FPAYMENT/CRYPTO METHODS ====================

    @Override
    @Transactional
    public FPaymentDepositResponse createDepositFPayment(Long userId, Long amountVnd, String ipAddress, String userAgent) {
        
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

        // Validate amount
        validateDepositAmount(amountVnd);

        // Generate unique transaction code
        Long transactionCode = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(100, 999);
        String requestId = String.valueOf(transactionCode);

        // Convert VND to USDT
        java.math.BigDecimal amountUsdt = fpaymentService.convertVndToUsdt(amountVnd);

        // Build callback URLs
        String callbackUrl = frontendUrl.replace("localhost:4200", "api.emailsieure.com") 
                + "/api/v1/wallets/fpayment/webhook";
        if (callbackUrl.contains("localhost")) {
            // For local testing, use a placeholder (webhook won't work locally)
            callbackUrl = "https://api.emailsieure.com/api/v1/wallets/fpayment/webhook";
        }
        String successUrl = frontendUrl + "/transactions";
        String cancelUrl = frontendUrl + "/transactions";

        // Call FPayment API to create invoice
        FPaymentApiResponse apiResponse = fpaymentService.createInvoice(
                "Nạp tiền vào ví EmailSieuRe",
                userEntity.getEmail(),
                amountUsdt,
                requestId,
                callbackUrl,
                successUrl,
                cancelUrl
        );

        // Create PENDING transaction
        TransactionEntity txn = TransactionEntity.builder()
                .transactionCode(transactionCode)
                .user(userEntity)
                .wallet(walletEntity)
                .type(TransactionTypeEnum.DEPOSIT)
                .amount(amountVnd)
                .balanceBefore(walletEntity.getBalance())
                .status(TransactionStatusEnum.PENDING)
                .description("Nạp tiền Crypto - " + amountUsdt + " USDT")
                .paymentMethod("FPAYMENT")
                .paymentReference(apiResponse.getData().getTransId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(txn);

        log.info("Created FPayment deposit - User: {}, AmountVnd: {}, AmountUsdt: {}, TransactionCode: {}", 
                userId, amountVnd, amountUsdt, transactionCode);

        return FPaymentDepositResponse.builder()
                .urlPayment(apiResponse.getData().getUrlPayment())
                .transId(apiResponse.getData().getTransId())
                .amountUsdt(amountUsdt)
                .amountVnd(amountVnd)
                .transactionCode(transactionCode)
                .exchangeRate(fpaymentProvider.getUsdVndRate())
                .build();
    }

    @Override
    @Transactional
    public void processFPaymentCallback(FPaymentWebhookDTO webhook) {
        if (webhook == null) {
            log.warn("Empty FPayment webhook received");
            return;
        }

        // Verify webhook credentials
        if (!fpaymentService.verifyWebhook(webhook.getMerchantId(), webhook.getApiKey())) {
            log.warn("Invalid FPayment webhook credentials");
            return;
        }

        String status = webhook.getStatus();
        String requestId = webhook.getRequestId();

        log.info("Processing FPayment callback - requestId: {}, status: {}", requestId, status);

        // Only process completed transactions
        if (!"completed".equalsIgnoreCase(status)) {
            log.debug("FPayment callback status is not completed: {}", status);
            return;
        }

        // Parse transaction code from request_id
        Long transactionCode;
        try {
            transactionCode = Long.parseLong(requestId);
        } catch (NumberFormatException e) {
            log.warn("Invalid request_id format in FPayment callback: {}", requestId);
            return;
        }

        // Check for duplicate using FPayment trans_id
        if (Strings.isNotBlank(webhook.getTransId())) {
            boolean exists = transactionRepository.existsByPaymentReference(webhook.getTransId());
            if (exists) {
                // Check if already SUCCESS
                Optional<TransactionEntity> existingTxn = transactionRepository.findByTransactionCode(transactionCode);
                if (existingTxn.isPresent() && existingTxn.get().getStatus() == TransactionStatusEnum.SUCCESS) {
                    log.debug("FPayment transaction already processed: {}", webhook.getTransId());
                    return;
                }
            }
        }

        // Find matching PENDING transaction
        Optional<TransactionEntity> optTxn = transactionRepository.findByTransactionCode(transactionCode);
        if (optTxn.isEmpty()) {
            log.debug("No matching transaction found for code: {}", transactionCode);
            return;
        }

        TransactionEntity txn = optTxn.get();

        // Skip if already processed
        if (txn.getStatus() == TransactionStatusEnum.SUCCESS) {
            log.debug("Transaction already processed: {}", transactionCode);
            return;
        }

        // Process successful deposit
        try {
            WalletEntity walletEntity = walletRepository.findByUserIdWithLock(txn.getUser().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

            // Use the VND amount from the original transaction (already calculated at creation)
            Long depositAmount = txn.getAmount();
            Long userId = txn.getUser().getId();

            // Calculate bonus based on user's rank
            Long bonusAmount = rankService.calculateDepositBonus(userId, depositAmount);
            Long totalAmount = depositAmount + bonusAmount;

            // Add to wallet
            walletEntity.addBalance(totalAmount);

            // Update transaction
            if (bonusAmount > 0) {
                txn.setDescription(txn.getDescription() + " (Bonus +" + bonusAmount + " VNĐ)");
            }
            txn.setBalanceAfter(walletEntity.getBalance());
            txn.setPaymentReference(webhook.getTransId());
            txn.markAsSuccess();

            walletRepository.save(walletEntity);
            transactionRepository.save(txn);

            log.info("FPayment deposit successful - User: {}, Amount: {}, Bonus: {}, Total: {}", 
                    userId, depositAmount, bonusAmount, totalAmount);

            // Send WebSocket notification to frontend
            depositNotifier.notifyDepositSuccess(
                    userId, 
                    transactionCode, 
                    depositAmount, 
                    bonusAmount, 
                    totalAmount, 
                    walletEntity.getBalance()
            );

        } catch (PessimisticLockException | CannotAcquireLockException e) {
            log.warn("Wallet locked during FPayment callback. Transaction: {}", transactionCode);
        }
    }

    // ==================== END FPAYMENT METHODS ====================

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

            //  Giao dịch thuộc user hiện tại
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Filter theo mã giao dịch (Long -> String for like search)
            if (Strings.isNotBlank(request.getTransactionCode())) {
                predicates.add(
                        cb.like(
                                root.get("transactionCode").as(String.class),
                                "%" + request.getTransactionCode().trim() + "%"));
            }

            //  From date
            if (request.getDateFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                request.getDateFrom()));
            }

            //  To date
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

        // Chỉ trừ tiền, không tạo transaction (lịch sử mua hàng đã lưu trong orders)
        walletEntity.deductBalance(amount);
        walletRepository.save(walletEntity);

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

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> searchAllTransactions(TransactionFilterDTO filter) {
        Sort sort = Utils.generatedSort(filter.getSort());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getLimit(), sort);

        Specification<TransactionEntity> spec = (Root<TransactionEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by transactionCode
            if (Strings.isNotBlank(filter.getTransactionCode())) {
                predicates.add(cb.equal(root.get("transactionCode"), Long.parseLong(filter.getTransactionCode())));
            }

            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), TransactionStatusEnum.fromKey(filter.getStatus())));
            }

            // Filter by type
            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), TransactionTypeEnum.fromKey(filter.getType())));
            }

            // Filter by email (search in user's email)
            if (Strings.isNotBlank(filter.getEmail())) {
                predicates.add(cb.like(cb.lower(root.get("user").get("email")), 
                    "%" + filter.getEmail().toLowerCase() + "%"));
            }

            // Filter by userId
            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
            }

            // Filter by amount range
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            // Filter by date range
            if (filter.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getDateFrom()));
            }
            if (filter.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getDateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TransactionEntity> page = transactionRepository.findAll(spec, pageable);
        return page.map(transactionMapper::toResponse);
    }

    @Override
    @Transactional
    public TransactionResponseDTO updateTransactionStatus(Long transactionId, Integer newStatus, String reason) {
        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        TransactionStatusEnum currentStatus = transaction.getStatus();
        TransactionStatusEnum targetStatus = TransactionStatusEnum.fromKey(newStatus);

        // Cho phép update từ PENDING hoặc FAILED (cho trường hợp timeout rồi admin duyệt lại)
        if (currentStatus != TransactionStatusEnum.PENDING && currentStatus != TransactionStatusEnum.FAILED) {
            throw new BusinessException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
        }

        // Nếu chuyển sang SUCCESS và là giao dịch DEPOSIT -> cộng tiền vào wallet + bonus + websocket
        if (targetStatus == TransactionStatusEnum.SUCCESS && transaction.getType() == TransactionTypeEnum.DEPOSIT) {
            WalletEntity wallet = walletRepository.findByUserIdWithLock(transaction.getUser().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

            Long depositAmount = transaction.getAmount();
            Long userId = transaction.getUser().getId();
            Long balanceBefore = wallet.getBalance();

            // Calculate bonus based on user's rank (giống processCassoTransaction)
            Long bonusAmount = rankService.calculateDepositBonus(userId, depositAmount);
            Long totalAmount = depositAmount + bonusAmount;

            // Add both deposit and bonus to wallet
            wallet.addBalance(totalAmount);
            wallet.setTotalDeposited(wallet.getTotalDeposited() + depositAmount);
            walletRepository.save(wallet);

            // Update transaction with bonus info
            if (bonusAmount > 0) {
                String originalDesc = transaction.getDescription() != null ? transaction.getDescription() : "Nạp tiền";
                transaction.setDescription(originalDesc + " (Bonus +" + bonusAmount + " VNĐ)");
            }

            // Update transaction với balance before/after
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(wallet.getBalance());
            transaction.markAsSuccess();

            log.info("Admin approved deposit - userId: {}, depositAmount: {}, bonus: {}, totalAmount: {}, balanceBefore: {}, balanceAfter: {}",
                    userId, depositAmount, bonusAmount, totalAmount, balanceBefore, wallet.getBalance());

            // Send WebSocket notification to frontend (giống processCassoTransaction)
            depositNotifier.notifyDepositSuccess(
                    userId,
                    transaction.getTransactionCode(),
                    depositAmount,
                    bonusAmount,
                    totalAmount,
                    wallet.getBalance()
            );

        } else if (targetStatus == TransactionStatusEnum.FAILED) {
            transaction.markAsFailed(reason != null ? reason : "Admin từ chối giao dịch");
            log.info("Admin rejected transaction - transactionId: {}, reason: {}", transactionId, reason);
        } else {
            // SUCCESS cho các loại khác (không phải DEPOSIT) - chỉ update status
            transaction.markAsSuccess();
        }

        transactionRepository.save(transaction);
        return transactionMapper.toResponse(transaction);
    }
}
