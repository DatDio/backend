package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.EmailLog;
import com.mailshop_dragonvu.enums.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Email Log Repository
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    /**
     * Find email logs by status
     */
    Page<EmailLog> findByEmailStatus(EmailStatus status, Pageable pageable);

    /**
     * Find email logs by recipient email
     */
    Page<EmailLog> findByRecipientEmail(String recipientEmail, Pageable pageable);

    /**
     * Find email logs by user
     */
    @Query("SELECT e FROM EmailLog e WHERE e.user.id = :userId")
    Page<EmailLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find failed emails for retry
     */
    @Query("SELECT e FROM EmailLog e WHERE e.emailStatus = 'FAILED' AND e.retryCount < :maxRetries")
    List<EmailLog> findFailedEmailsForRetry(@Param("maxRetries") Integer maxRetries);

    /**
     * Find email logs by date range
     */
    @Query("SELECT e FROM EmailLog e WHERE e.sentAt BETWEEN :startDate AND :endDate")
    Page<EmailLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate, 
                                   Pageable pageable);

    /**
     * Count emails by status
     */
    Long countByEmailStatus(EmailStatus status);
}
