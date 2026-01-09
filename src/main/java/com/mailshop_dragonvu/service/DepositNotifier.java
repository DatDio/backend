package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.websocket.DepositSuccessMessage;
import com.mailshop_dragonvu.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Service to notify frontend about successful deposits via WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepositNotifier {

    private static final String DESTINATION_TEMPLATE = "/topic/deposit/%d";

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    /**
     * Send deposit success notification after transaction commits
     * Uses user-specific topic so only the depositing user receives the message
     */
    public void notifyDepositSuccess(Long userId, Long transactionCode, Long amount, 
                                      Long bonusAmount, Long totalAmount, Long newBalance) {
        if (userId == null) {
            return;
        }

        DepositSuccessMessage message = DepositSuccessMessage.builder()
                .userId(userId)
                .transactionCode(transactionCode)
                .amount(amount)
                .bonusAmount(bonusAmount)
                .totalAmount(totalAmount)
                .newBalance(newBalance)
                .message(messageService.getMessage(MessageKeys.Notification.DEPOSIT_SUCCESS))
                .build();

        // Send after transaction commits to ensure data consistency
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendMessage(userId, message);
                }
            });
        } else {
            sendMessage(userId, message);
        }
    }

    private void sendMessage(Long userId, DepositSuccessMessage message) {
        try {
            String destination = String.format(DESTINATION_TEMPLATE, userId);
            messagingTemplate.convertAndSend(destination, message);
            log.info("Sent deposit success notification to user {} via WebSocket", userId);
        } catch (Exception ex) {
            log.error("Failed to send deposit notification to user {}: {}", userId, ex.getMessage(), ex);
        }
    }
}
