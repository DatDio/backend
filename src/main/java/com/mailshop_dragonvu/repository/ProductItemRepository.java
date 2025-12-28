package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.enums.WarehouseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItemEntity, Long>, JpaSpecificationExecutor<ProductItemEntity> {

    // ==================== COUNTING ====================
    
    // Đếm tất cả items chưa bán và chưa hết hạn (cả 2 kho)
    @Query("SELECT COUNT(pi) FROM ProductItemEntity pi WHERE pi.product.id = :productId AND pi.sold = false AND pi.expired = false")
    long countAvailableItems(@Param("productId") Long productId);

    // Lấy tất cả items chưa bán (dùng để check trùng email)
    List<ProductItemEntity> findByProductIdAndSoldFalse(Long productId);

    // Đếm items trong kho PHỤ (SECONDARY) - hiển thị cho khách hàng, chưa hết hạn
    @Query("SELECT COUNT(pi) FROM ProductItemEntity pi WHERE pi.product.id = :productId AND pi.sold = false AND pi.expired = false AND pi.warehouseType = 'SECONDARY'")
    long countSecondaryItems(@Param("productId") Long productId);

    // Đếm items trong kho CHÍNH (PRIMARY) - ẩn với khách hàng, chưa hết hạn
    @Query("SELECT COUNT(pi) FROM ProductItemEntity pi WHERE pi.product.id = :productId AND pi.sold = false AND pi.expired = false AND pi.warehouseType = 'PRIMARY'")
    long countPrimaryItems(@Param("productId") Long productId);

    // ==================== FETCHING FOR SALE ====================
    
    // Lấy random items từ kho PHỤ để bán (chỉ bán từ kho phụ, chưa hết hạn)
    @Query(value = """
        SELECT * FROM product_items
        WHERE product_id = :productId AND sold = false AND expired = false AND warehouse_type = 'SECONDARY'
        ORDER BY RAND()
        LIMIT :quantity
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<ProductItemEntity> findRandomUnsoldSecondaryItems(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

    // Lấy items từ kho CHÍNH để chuyển sang kho PHỤ (chưa hết hạn)
    @Query(value = """
        SELECT * FROM product_items
        WHERE product_id = :productId AND sold = false AND expired = false AND warehouse_type = 'PRIMARY'
        ORDER BY id ASC
        LIMIT :quantity
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<ProductItemEntity> findItemsToTransfer(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

    // Legacy: Lấy random items (cả 2 kho) - giữ lại cho backward compatibility
    @Query(value = """
        SELECT * FROM product_items
        WHERE product_id = :productId AND sold = false AND expired = false
        ORDER BY RAND()
        LIMIT :quantity
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<ProductItemEntity> findRandomUnsoldItems(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

    // ==================== EXPIRATION ====================
    
    // Tìm items đã hết hạn (expires_at < now VÀ chưa bị đánh dấu) để đánh dấu expired
    @Query(value = """
        SELECT * FROM product_items
        WHERE product_id = :productId 
          AND sold = false 
          AND expired = false
          AND expires_at IS NOT NULL
          AND expires_at < NOW()
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<ProductItemEntity> findExpiredItems(@Param("productId") Long productId);

    // Batch đánh dấu expired
    @Modifying
    @Query("UPDATE ProductItemEntity pi SET pi.expired = true, pi.expiredAt = CURRENT_TIMESTAMP WHERE pi.id IN :ids")
    void markAsExpired(@Param("ids") List<Long> ids);

    // ==================== UPDATING ====================
    
    @Modifying
    @Query("UPDATE ProductItemEntity pi SET pi.sold = true, pi.buyerId = :buyerId, pi.soldAt = CURRENT_TIMESTAMP WHERE pi.id = :id")
    void markAsSold(@Param("id") Long id, @Param("buyerId") Long buyerId);

    // Batch update warehouse type (chuyển từ PRIMARY sang SECONDARY)
    @Modifying
    @Query("UPDATE ProductItemEntity pi SET pi.warehouseType = :warehouseType WHERE pi.id IN :ids")
    void updateWarehouseType(@Param("ids") List<Long> ids, @Param("warehouseType") WarehouseType warehouseType);

    // ==================== OTHER QUERIES ====================
    
    Page<ProductItemEntity> findByProduct_Id(Long productId, Pageable pageable);

    List<ProductItemEntity> findByProductIdAndAccountDataIn(Long productId, java.util.Collection<String> accountData);

    // ==================== BULK OPERATIONS ====================
    
    // Xóa nhiều items theo danh sách account data
    @Modifying
    @Query("DELETE FROM ProductItemEntity pi WHERE pi.product.id = :productId AND pi.accountData IN :accountDataList")
    int deleteByAccountDataIn(@Param("productId") Long productId, @Param("accountDataList") java.util.Collection<String> accountDataList);
    
    // Lấy TẤT CẢ items đã hết hạn (không lock - dùng cho export/delete)
    @Query(value = """
        SELECT * FROM product_items
        WHERE product_id = :productId 
          AND sold = false 
          AND (expired = true 
               OR (expires_at IS NOT NULL AND expires_at < NOW()))
    """, nativeQuery = true)
    List<ProductItemEntity> findAllExpiredItems(@Param("productId") Long productId);
    
    // Xóa tất cả items đã hết hạn
    @Modifying
    @Query(value = """
        DELETE FROM product_items
        WHERE product_id = :productId 
          AND sold = false 
          AND (expired = true 
               OR (expires_at IS NOT NULL AND expires_at < NOW()))
    """, nativeQuery = true)
    int deleteAllExpiredItems(@Param("productId") Long productId);
}

