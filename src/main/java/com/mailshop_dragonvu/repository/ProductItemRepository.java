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

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItemEntity, Long>, JpaSpecificationExecutor<ProductItemEntity> {

    @Query("SELECT COUNT(pi) FROM ProductItemEntity pi WHERE pi.product.id = :productId AND pi.sold = false")
    long countAvailableItems(@Param("productId") Long productId);

    //Lay random tai khoan theo so luong muon lay, khoa row de tranh double-sell
    @Query(value = """
    SELECT * FROM product_items
    WHERE product_id = :productId AND sold = false
    ORDER BY RAND()
    LIMIT :quantity
    FOR UPDATE SKIP LOCKED
""", nativeQuery = true)
    List<ProductItemEntity> findRandomUnsoldItems(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

    @Modifying
    @Query("UPDATE ProductItemEntity pi SET pi.sold = true, pi.buyerId = :buyerId, pi.soldAt = CURRENT_TIMESTAMP WHERE pi.id = :id")
    void markAsSold(@Param("id") Long id, @Param("buyerId") Long buyerId);

    Page<ProductItemEntity> findByProduct_Id(Long productId, Pageable pageable);

    List<ProductItemEntity> findByProductIdAndAccountDataIn(Long productId, java.util.Collection<String> accountData);
}
