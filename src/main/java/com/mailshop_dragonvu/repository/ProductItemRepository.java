package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.ProductItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItemEntity, Long>, JpaSpecificationExecutor<ProductItemEntity> {

    @Query("SELECT COUNT(pi) FROM ProductItemEntity pi WHERE pi.product.id = :productId AND pi.sold = false")
    long countAvailableItems(@Param("productId") Long productId);

    @Query("SELECT pi FROM ProductItemEntity pi WHERE pi.product.id = :productId AND pi.sold = false ORDER BY pi.id ASC LIMIT 1")
    Optional<ProductItemEntity> findUnsoldItem(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE ProductItemEntity pi SET pi.sold = true, pi.buyerId = :buyerId, pi.orderId = :orderId, pi.soldAt = CURRENT_TIMESTAMP WHERE pi.id = :id")
    void markAsSold(@Param("id") Long id, @Param("buyerId") Long buyerId, @Param("orderId") Long orderId);

    Page<ProductItemEntity> findByProduct_Id(Long productId, Pageable pageable);
}
