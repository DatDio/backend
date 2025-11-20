package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Wallet Repository
 */
@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    /**
     * Find wallet by user ID with pessimistic lock (prevent race conditions)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.user.id = :userId")
    Optional<WalletEntity> findByUserIdWithLock(@Param("userId") Long userId);


    @Query("SELECT w FROM WalletEntity w WHERE w.user.id = :userId")
    Optional<WalletEntity> findByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WalletEntity w WHERE w.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
