package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.TransactionStatusEnum;
import com.mailshop_dragonvu.enums.TransactionTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Transaction Entity - Wallet transaction history
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity extends BaseEntity {

    @Column(name = "transaction_code", unique = true, nullable = false)
    private Long transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @Column(name = "type", nullable = false)
    private TransactionTypeEnum type;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private Long amount;

    @Column(name = "balance_before", precision = 15, scale = 2)
    private Long balanceBefore;

    @Column(name = "balance_after", precision = 15, scale = 2)
    private Long balanceAfter;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private TransactionStatusEnum status = TransactionStatusEnum.PENDING;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 255)
    private String paymentReference;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * Mark transaction as successful
     */
    public void markAsSuccess() {
        this.status = TransactionStatusEnum.SUCCESS;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Mark transaction as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = TransactionStatusEnum.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}
