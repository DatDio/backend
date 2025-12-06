package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.authProvider = :provider AND u.providerId = :providerId")
    Optional<UserEntity> findByAuthProviderAndProviderId(AuthProvider provider, String providerId);

    /**
     * Đếm số user mới đăng ký trong khoảng thời gian
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Long countNewUsersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

