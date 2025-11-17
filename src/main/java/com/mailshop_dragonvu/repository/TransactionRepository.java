package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.Transaction;
import com.mailshop_dragonvu.enums.TransactionStatus;
import com.mailshop_dragonvu.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transaction by code
     */
    Optional<Transaction> findByTransactionCode(String transactionCode);

    /**
     * Find transaction by PayOS order code
     */
    Optional<Transaction> findByPayosOrderCode(Long payosOrderCode);

    /**
     * Find user transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find user transactions by type
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndType(@Param("userId") Long userId, 
                                         @Param("type") TransactionType type, 
                                         Pageable pageable);

    /**
     * Find user transactions by status
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.status = :status ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndStatus(@Param("userId") Long userId, 
                                           @Param("status") TransactionStatus status, 
                                           Pageable pageable);

    /**
     * Count pending transactions for user (anti-spam check)
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.status = 'PENDING' AND t.createdAt > :since")
    Long countPendingTransactionsSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Find duplicate transactions by amount and user in timeframe (anti-cheat)
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.amount = :amount AND t.status IN ('PENDING', 'PROCESSING') AND t.createdAt > :since")
    List<Transaction> findDuplicateTransactions(@Param("userId") Long userId, 
                                                @Param("amount") java.math.BigDecimal amount, 
                                                @Param("since") LocalDateTime since);

    /**
     * Count transactions by IP in timeframe (DDoS detection)
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.ipAddress = :ipAddress AND t.createdAt > :since")
    Long countTransactionsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
}
