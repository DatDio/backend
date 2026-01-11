package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.TransactionEntity;
import com.mailshop_dragonvu.enums.TransactionStatusEnum;
import com.mailshop_dragonvu.enums.TransactionTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Repository
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long>, JpaSpecificationExecutor<TransactionEntity> {

    void deleteByTransactionCode(Long transactionCode);
    /**
     * Find transactionEntity by code
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.transactionCode = :transactionCode")
    Optional<TransactionEntity> findByTransactionCode(@Param("transactionCode") Long transactionCode);

    /**
     * Find userEntity transactions
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    Page<TransactionEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find userEntity transactions by type
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.user.id = :userId AND t.type = :type ORDER BY t.createdAt DESC")
    Page<TransactionEntity> findByUserIdAndType(@Param("userId") Long userId,
                                                @Param("type") TransactionTypeEnum type,
                                                Pageable pageable);

    /**
     * Find userEntity transactions by status
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.user.id = :userId AND t.status = :status ORDER BY t.createdAt DESC")
    Page<TransactionEntity> findByUserIdAndStatus(@Param("userId") Long userId,
                                                  @Param("status") TransactionStatusEnum status,
                                                  Pageable pageable);

    /**
     * Count pending transactions for userEntity (anti-spam check)
     */
    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.user.id = :userId AND t.status = com.mailshop_dragonvu.enums.TransactionStatusEnum.PENDING AND t.createdAt > :since")
    Long countPendingTransactionsSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Find duplicate transactions by amount and userEntity in timeframe (anti-cheat)
     */
    @Query("""
                SELECT t FROM TransactionEntity t
                WHERE t.user.id = :userId
                  AND t.amount = :amount
                  AND t.status IN (
                       com.mailshop_dragonvu.enums.TransactionStatusEnum.PENDING,
                       com.mailshop_dragonvu.enums.TransactionStatusEnum.PROCESSING
                  )
                  AND t.createdAt > :since
            """)
    List<TransactionEntity> findDuplicateTransactions(@Param("userId") Long userId,
                                                      @Param("amount") Long amount,
                                                      @Param("since") LocalDateTime since);

    /**
     * Count transactions by IP in timeframe (DDoS detection)
     */
    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.ipAddress = :ipAddress AND t.createdAt > :since")
    Long countTransactionsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    /**
     * Calculate total successful deposit amount for a user within a time period
     * Used for rank calculation
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t " +
           "WHERE t.user.id = :userId " +
           "AND t.type = com.mailshop_dragonvu.enums.TransactionTypeEnum.DEPOSIT " +
           "AND t.status = com.mailshop_dragonvu.enums.TransactionStatusEnum.SUCCESS " +
           "AND t.createdAt >= :since")
    Long getTotalDepositInPeriod(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Check if transaction with payment reference exists (for deduplication)
     */
    boolean existsByPaymentReference(String paymentReference);

    /**
     * Find transactions by status created before a certain time (for timeout scheduler)
     */
    List<TransactionEntity> findByStatusAndCreatedAtBefore(TransactionStatusEnum status, LocalDateTime cutoffTime);

    /**
     * Find pending FPayment transactions for polling
     * Only returns transactions created after cutoffTime (within polling window)
     */
    @Query("SELECT t FROM TransactionEntity t " +
           "WHERE t.status = :status " +
           "AND t.paymentMethod = :paymentMethod " +
           "AND t.createdAt > :cutoffTime " +
           "ORDER BY t.createdAt ASC")
    List<TransactionEntity> findPendingFPaymentTransactions(
            @Param("status") TransactionStatusEnum status,
            @Param("paymentMethod") String paymentMethod,
            @Param("cutoffTime") LocalDateTime cutoffTime);
}
