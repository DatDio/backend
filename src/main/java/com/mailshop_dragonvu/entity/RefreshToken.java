package com.mailshop_dragonvu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "REFRESH_TOKENS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "base_seq_gen", sequenceName = "REFRESH_TOKEN_SEQ", allocationSize = 1)
public class RefreshToken extends BaseEntity {

    @Column(name = "TOKEN", nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "EXPIRY_DATE", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "REVOKED")
    @Builder.Default
    private Boolean revoked = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

}
