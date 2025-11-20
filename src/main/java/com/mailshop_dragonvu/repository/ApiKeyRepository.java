package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.ApiKeyEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * API Key Repository
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {

    /**
     * Find all API keys for a user
     */
    List<ApiKeyEntity> findByUserOrderByCreatedAtDesc(UserEntity userEntity);

    /**
     * Find all API keys by user ID
     */
    @Query("SELECT a FROM ApiKeyEntity a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<ApiKeyEntity> findByUserId(@Param("userId") Long userId);

    /**
     * Find active API keys for a user
     */
    @Query("SELECT a FROM ApiKeyEntity a WHERE a.user.id = :userId AND a.status = com.mailshop_dragonvu.enums.ApiKeyStatusEnum.ACTIVE ORDER BY a.createdAt DESC")
    List<ApiKeyEntity> findActiveKeysByUserId(@Param("userId") Long userId);

    /**
     * Count active API keys for a user
     */
    @Query("SELECT COUNT(a) FROM ApiKeyEntity a WHERE a.user.id = :userId AND a.status = com.mailshop_dragonvu.enums.ApiKeyStatusEnum.ACTIVE")
    Long countActiveKeysByUserId(@Param("userId") Long userId);

    /**
     * Find API key by ID and user
     */
    Optional<ApiKeyEntity> findByIdAndUser(Long id, UserEntity userEntity);

    /**
     * Check if user has any active API keys
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM ApiKeyEntity a WHERE a.user.id = :userId AND a.status = com.mailshop_dragonvu.enums.ApiKeyStatusEnum.ACTIVE")
    boolean hasActiveKeys(@Param("userId") Long userId);

}
