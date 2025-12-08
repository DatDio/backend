package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.RankEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Rank Repository - Access rank data
 */
@Repository
public interface RankRepository extends JpaRepository<RankEntity, Long>, JpaSpecificationExecutor<RankEntity> {

    /**
     * Find all active ranks ordered by display order
     */
    List<RankEntity> findAllByStatusOrderByDisplayOrderAsc(ActiveStatusEnum status);

    /**
     * Find rank by name
     */
    Optional<RankEntity> findByName(String name);

    /**
     * Check if rank name already exists
     */
    boolean existsByName(String name);

    /**
     * Find the best matching rank for a given deposit amount
     * Returns the rank with highest minDeposit that is <= the given amount
     */
    @Query("SELECT r FROM RankEntity r WHERE r.status = :status AND r.minDeposit <= :amount ORDER BY r.minDeposit DESC LIMIT 1")
    Optional<RankEntity> findBestMatchingRank(@Param("status") ActiveStatusEnum status, @Param("amount") Long amount);

    /**
     * Find all active ranks ordered by minDeposit (for display)
     */
    @Query("SELECT r FROM RankEntity r WHERE r.status = :status ORDER BY r.minDeposit ASC")
    List<RankEntity> findAllActiveOrderByMinDeposit(@Param("status") ActiveStatusEnum status);
}
