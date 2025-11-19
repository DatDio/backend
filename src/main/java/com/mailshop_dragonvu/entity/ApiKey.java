package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.ApiKeyStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * API Key Entity - stores hashed API keys for user authentication
 */
@Entity
@Table(name = "api_keys", indexes = {
        @Index(name = "idx_api_key_user", columnList = "user_id"),
        @Index(name = "idx_api_key_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "key_hash", nullable = false, length = 255)
    private String keyHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ApiKeyStatus status = ApiKeyStatus.ACTIVE;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "name", length = 100)
    private String name;

    /** Check if API key is expired */
    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }

    /** Check if API key is active and not expired */
    public boolean isValid() {
        return status == ApiKeyStatus.ACTIVE && !isExpired();
    }

    /** Update last used timestamp */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
