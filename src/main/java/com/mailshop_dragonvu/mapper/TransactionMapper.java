package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.transactions.TransactionResponseDTO;
import com.mailshop_dragonvu.entity.TransactionEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Transaction Mapper
 */
@Component
public class TransactionMapper {

    public TransactionResponseDTO toResponse(TransactionEntity transactionEntity) {
        if (transactionEntity == null) {
            return null;
        }

        return TransactionResponseDTO.builder()
                .id(transactionEntity.getId())
                .transactionCode(mapTransactionCode(transactionEntity.getTransactionCode()))
                .userId(transactionEntity.getUser() != null ? transactionEntity.getUser().getId() : null)
                .userEmail(transactionEntity.getUser() != null ? transactionEntity.getUser().getEmail() : null)
                .type(transactionEntity.getType() != null ? String.valueOf(transactionEntity.getType()) : null)
                .amount(toBigDecimal(transactionEntity.getAmount()))
                .balanceBefore(toBigDecimal(transactionEntity.getBalanceBefore()))
                .balanceAfter(toBigDecimal(transactionEntity.getBalanceAfter()))
                .status(transactionEntity.getStatus() != null ? String.valueOf(transactionEntity.getStatus()) : null)
                .description(transactionEntity.getDescription())
                .paymentMethod(transactionEntity.getPaymentMethod())
                .paymentReference(transactionEntity.getPaymentReference())
                .createdAt(transactionEntity.getCreatedAt())
                .completedAt(transactionEntity.getCompletedAt())
                .build();
    }

    private String mapTransactionCode(Long transactionCode) {
        return transactionCode != null ? transactionCode.toString() : null;
    }

    private BigDecimal toBigDecimal(Long value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}
