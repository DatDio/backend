package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.RefreshTokenEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @Modifying
    @Query("UPDATE RefreshTokenEntity t SET t.revoked = true WHERE t.user = :user")
    void revokeAllByUser(@Param("user") UserEntity user);


    Optional<RefreshTokenEntity> findByUser(UserEntity userEntity);

    Optional<RefreshTokenEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") UserEntity userEntity);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

}
