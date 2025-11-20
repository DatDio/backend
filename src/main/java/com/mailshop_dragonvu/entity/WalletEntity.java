package com.mailshop_dragonvu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Đối tượng ví - Số dư của người dùng (dùng Long)
 * Quy đổi: số tiền lưu theo VND — dùng Long cho hiệu năng + chính xác.
 */
@Entity
@Table(name = "wallets")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WalletEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private Long balance = 0L;

    @Column(name = "total_deposited")
    @Builder.Default
    private Long totalDeposited = 0L;

    @Column(name = "total_spent")
    @Builder.Default
    private Long totalSpent = 0L;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "lock_reason", length = 500)
    private String lockReason;

    /**
     * Thêm số dư vào ví
     */
    public void addBalance(Long amount) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Số tiền nạp không hợp lệ");

        this.balance += amount;
        this.totalDeposited += amount;
    }

    /**
     * Trừ số dư khỏi ví
     */
    public void deductBalance(Long amount) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Số tiền trừ không hợp lệ");

        if (this.balance < amount)
            throw new IllegalStateException("Số dư không đủ");

        this.balance -= amount;
        this.totalSpent += amount;
    }

    /**
     * Check if wallet has sufficient balance
     */
    public boolean hasSufficientBalance(Long amount) {
        return amount != null && this.balance >= amount;
    }
}
