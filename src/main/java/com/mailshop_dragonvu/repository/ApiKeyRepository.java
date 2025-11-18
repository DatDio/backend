package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.ApiKey;
import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.enums.ApiKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * API Key Repository
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    /**
     * Find all API keys for a user
     */
    List<ApiKey> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find all API keys by user ID
     */
    @Query("SELECT a FROM ApiKey a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<ApiKey> findByUserId(@Param("userId") Long userId);

    /**
     * Find active API keys for a user
     */
    @Query("SELECT a FROM ApiKey a WHERE a.user.id = :userId AND a.status = 'ACTIVE' ORDER BY a.createdAt DESC")
    List<ApiKey> findActiveKeysByUserId(@Param("userId") Long userId);

    /**
     * Count active API keys for a user
     */
    @Query("SELECT COUNT(a) FROM ApiKey a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
    Long countActiveKeysByUserId(@Param("userId") Long userId);

    /**
     * Find API key by ID and user
     */
    Optional<ApiKey> findByIdAndUser(Long id, User user);

    /**
     * Check if user has any active API keys
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM ApiKey a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
    boolean hasActiveKeys(@Param("userId") Long userId);

    /**
     * Find all expired API keys that are still active
     */
    @Query("SELECT a FROM ApiKey a WHERE a.status = 'ACTIVE' AND a.expiredAt IS NOT NULL AND a.expiredAt < CURRENT_TIMESTAMP")
    List<ApiKey> findExpiredActiveKeys();
}
