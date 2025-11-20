package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.EmailLogEntity;
import com.mailshop_dragonvu.enums.EmailStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Email Log Repository
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLogEntity, Long>, JpaSpecificationExecutor<EmailLogEntity> {

    /**
     * Find email logs by status
     */
    Page<EmailLogEntity> findByEmailStatus(EmailStatusEnum status, Pageable pageable);

    /**
     * Find email logs by recipient email
     */
    Page<EmailLogEntity> findByRecipientEmail(String recipientEmail, Pageable pageable);

    /**
     * Find email logs by user
     */
    @Query("SELECT e FROM EmailLogEntity e WHERE e.userEntity.id = :userId")
    Page<EmailLogEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find failed emails for retry
     */
    @Query("SELECT e FROM EmailLogEntity e WHERE e.emailStatus = com.mailshop_dragonvu.enums.EmailStatusEnum.FAILED AND e.retryCount < :maxRetries")
    List<EmailLogEntity> findFailedEmailsForRetry(@Param("maxRetries") Integer maxRetries);

    /**
     * Find email logs by date range
     */
    @Query("SELECT e FROM EmailLogEntity e WHERE e.sentAt BETWEEN :startDate AND :endDate")
    Page<EmailLogEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    /**
     * Count emails by status
     */
    Long countByEmailStatus(EmailStatusEnum status);

}
