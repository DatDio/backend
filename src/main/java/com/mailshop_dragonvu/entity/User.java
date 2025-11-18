package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.AuthProvider;
import com.mailshop_dragonvu.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "base_seq_gen", sequenceName = "USER_SEQ", allocationSize = 1)
public class User extends BaseEntity {

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Column(name = "FULL_NAME", length = 100)
    private String fullName;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Column(name = "AVATAR_URL", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "AUTH_PROVIDER", length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "PROVIDER_ID", length = 100)
    private String providerId;

    @Column(name = "EMAIL_VERIFIED")
    @Builder.Default
    private Boolean emailVerified = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USER_ROLES",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @Column(name = "STATUS", length = 20, nullable = false)
    private String status = "ACTIVE";
}
