package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.TransactionStatus;
import com.mailshop_dragonvu.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Entity - Wallet transaction history
 */
@Entity
@Table(name = "TRANSACTIONS")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @Column(name = "TRANSACTION_CODE", unique = true, nullable = false, length = 50)
    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WALLET_ID", nullable = false)
    private Wallet wallet;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "AMOUNT", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "BALANCE_BEFORE", precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "BALANCE_AFTER", precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "PAYMENT_METHOD", length = 50)
    private String paymentMethod;

    @Column(name = "PAYMENT_REFERENCE", length = 255)
    private String paymentReference;

    @Column(name = "PAYOS_ORDER_CODE")
    private Long payosOrderCode;

    @Column(name = "IP_ADDRESS", length = 45)
    private String ipAddress;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    @Column(name = "ERROR_MESSAGE", length = 1000)
    private String errorMessage;

    /**
     * Mark transaction as successful
     */
    public void markAsSuccess() {
        this.status = TransactionStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Mark transaction as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = TransactionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}
