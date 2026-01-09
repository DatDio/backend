package com.mailshop_dragonvu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "auth_provider", length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private RefreshTokenEntity refreshToken;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private ActiveStatusEnum status = ActiveStatusEnum.ACTIVE;

    @Column(name = "is_collaborator")
    @Builder.Default
    private Boolean isCollaborator = false;

    @Column(name = "bonus_percent")
    @Builder.Default
    private Integer bonusPercent = 0;  // 0-100% bonus khi nạp tiền
}
