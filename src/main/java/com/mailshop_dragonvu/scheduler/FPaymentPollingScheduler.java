//package com.mailshop_dragonvu.scheduler;
//
//import com.mailshop_dragonvu.entity.TransactionEntity;
//import com.mailshop_dragonvu.enums.TransactionStatusEnum;
//import com.mailshop_dragonvu.repository.TransactionRepository;
//import com.mailshop_dragonvu.service.WalletService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * Scheduler tự động poll trạng thái các giao dịch FPayment đang PENDING
// * Chạy mỗi 15 giây để kiểm tra và xử lý khi thanh toán hoàn tất
// * WebSocket sẽ tự động notify frontend khi deposit thành công
// */
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class FPaymentPollingScheduler {
//
//    private final TransactionRepository transactionRepository;
//    private final WalletService walletService;
//
//    // Only poll transactions created within last 30 minutes
//    private static final int MAX_POLLING_MINUTES = 30;
//
//    /**
//     * Chạy mỗi 15 giây để kiểm tra FPayment transactions
//     */
//    @Scheduled(fixedRateString = "${app.fpayment.polling.interval:15000}") // 15 giây
//    public void pollPendingFPaymentTransactions() {
//        // Only get PENDING FPayment transactions created within last 30 minutes
//        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(MAX_POLLING_MINUTES);
//
//        List<TransactionEntity> pendingTransactions = transactionRepository
//                .findPendingFPaymentTransactions(TransactionStatusEnum.PENDING, "FPAYMENT", cutoffTime);
//
//        if (pendingTransactions.isEmpty()) {
//            return;
//        }
//
//        log.debug("Polling {} pending FPayment transactions", pendingTransactions.size());
//
//        for (TransactionEntity transaction : pendingTransactions) {
//            try {
//                String status = walletService.checkFPaymentStatus(transaction.getTransactionCode());
//                log.debug("FPayment poll result - TransactionCode: {}, Status: {}",
//                        transaction.getTransactionCode(), status);
//            } catch (Exception e) {
//                log.warn("Error polling FPayment status for transaction {}: {}",
//                        transaction.getTransactionCode(), e.getMessage());
//            }
//        }
//    }
//}
