package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.ApiKeyStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API Key Entity - stores hashed API keys for user authentication
 */
@Entity
@Table(name = "API_KEYS", indexes = {
        @Index(name = "IDX_API_KEY_USER", columnList = "USER_ID"),
        @Index(name = "IDX_API_KEY_STATUS", columnList = "STATUS")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "KEY_HASH", nullable = false, length = 255)
    private String keyHash;


    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApiKeyStatus status = ApiKeyStatus.ACTIVE;

    @Column(name = "EXPIRED_AT")
    private LocalDateTime expiredAt;

    @Column(name = "LAST_USED_AT")
    private LocalDateTime lastUsedAt;

    @Column(name = "NAME", length = 100)
    private String name;


    /**
     * Check if API key is expired
     */
    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }

    /**
     * Check if API key is active and not expired
     */
    public boolean isValid() {
        return status == ApiKeyStatus.ACTIVE && !isExpired();
    }

    /**
     * Update last used timestamp
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
