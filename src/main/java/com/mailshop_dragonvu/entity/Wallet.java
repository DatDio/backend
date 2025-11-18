package com.mailshop_dragonvu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Đối tượng ví - Số dư của người dùng
 */
@Entity
@Table(name = "WALLETS")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @Column(name = "BALANCE", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "TOTAL_DEPOSITED", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalDeposited = BigDecimal.ZERO;

    @Column(name = "TOTAL_SPENT", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "IS_LOCKED", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "LOCK_REASON", length = 500)
    private String lockReason;

    /**
     * Thêm số dư vào ví
     */
    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.totalDeposited = this.totalDeposited.add(amount);
    }

    /**
     * Trừ số dư khỏi ví
     */
    public void deductBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Số dư không đủ");
        }
        this.balance = this.balance.subtract(amount);
        this.totalSpent = this.totalSpent.add(amount);
    }

    /**
     * Check if wallet has sufficient balance
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}
