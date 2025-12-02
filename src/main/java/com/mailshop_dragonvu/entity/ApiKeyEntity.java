package com.mailshop_dragonvu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mailshop_dragonvu.enums.ApiKeyStatusEnum;
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
        @Index(name = "idx_api_key_status", columnList = "status"),
        @Index(name = "idx_api_key_prefix", columnList = "prefix")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;

    @Column(name = "key_hash", nullable = false, length = 255)
    private String keyHash;


    @Column(name = "status", nullable = false)
    @Builder.Default
    private ApiKeyStatusEnum status = ApiKeyStatusEnum.ACTIVE;

    @Column(name = "prefix", nullable = false, length = 24)
    private String prefix;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "name", length = 100)
    private String name;


    @JsonIgnore
    public boolean isValid() {
        return status == ApiKeyStatusEnum.ACTIVE;
    }

    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
