package com.mailshop_dragonvu.scheduler;

import com.mailshop_dragonvu.entity.TransactionEntity;
import com.mailshop_dragonvu.enums.TransactionStatusEnum;
import com.mailshop_dragonvu.repository.TransactionRepository;
import com.mailshop_dragonvu.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler tự động đánh dấu FAILED cho các giao dịch PENDING quá thời gian quy định
 * Chạy mỗi 1 phút
 * Thời gian timeout có thể cấu hình qua Admin Settings (key: scheduler.transaction_timeout_minutes)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionTimeoutScheduler {

    private final TransactionRepository transactionRepository;
    private final SystemSettingService systemSettingService;
    
    // Default timeout: 10 phút
    private static final int DEFAULT_TIMEOUT_MINUTES = 10;
    private static final String SETTING_KEY = "scheduler.transaction_timeout_minutes";

    /**
     * Chạy mỗi 1 phút để kiểm tra transaction timeout
     */
    @Scheduled(fixedRateString = "${app.transaction.timeout.interval:60000}") // 1 phút = 60000ms
    @Transactional
    public void markTimeoutTransactions() {
        // Đọc timeout từ database settings
        int timeoutMinutes = systemSettingService.getIntValue(SETTING_KEY, DEFAULT_TIMEOUT_MINUTES);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        
        List<TransactionEntity> pendingTransactions = transactionRepository
                .findByStatusAndCreatedAtBefore(TransactionStatusEnum.PENDING, cutoffTime);

        if (pendingTransactions.isEmpty()) {
            return;
        }

        log.info("Found {} PENDING transactions older than {} minutes", 
                pendingTransactions.size(), timeoutMinutes);

        for (TransactionEntity transaction : pendingTransactions) {
            transaction.markAsFailed("Quá thời gian thanh toán");
            transactionRepository.save(transaction);
            
            log.info("Marked transaction {} as FAILED due to timeout - User: {}, Amount: {}", 
                    transaction.getTransactionCode(), 
                    transaction.getUser().getEmail(),
                    transaction.getAmount());
        }
        
        log.info("Marked {} transactions as FAILED due to timeout", pendingTransactions.size());
    }
}
