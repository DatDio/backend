package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.ExpirationType;
import com.mailshop_dragonvu.enums.WarehouseType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_items", indexes = {
        @Index(name = "idx_product_items_product_id", columnList = "product_id"),
        @Index(name = "idx_product_items_sold", columnList = "sold"),
        @Index(name = "idx_product_items_warehouse_type", columnList = "warehouse_type")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "account_data", nullable = false, columnDefinition = "TEXT")
    private String accountData; // Ví dụ: "mail|pass|recovery|token..."

    @Column(nullable = false)
    @Builder.Default
    private Boolean sold = false;

    // Loại kho: PRIMARY (kho chính, ẩn) hoặc SECONDARY (kho phụ, hiển thị)
    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", nullable = false, length = 20)
    @Builder.Default
    private WarehouseType warehouseType = WarehouseType.PRIMARY;

    @Column(name = "buyer_id")
    private Long buyerId;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    // Loại thời gian hết hạn (3h, 6h, 1 tháng, 6 tháng, không hạn)
    @Enumerated(EnumType.STRING)
    @Column(name = "expiration_type", length = 20)
    @Builder.Default
    private ExpirationType expirationType = ExpirationType.NONE;

    // Thời điểm hết hạn (tính từ createdAt + expirationType.hours)
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Trạng thái đã bị đánh dấu hết hạn
    @Column(nullable = false)
    @Builder.Default
    private Boolean expired = false;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    public void markSold(Long buyerId) {
        this.sold = true;
        this.buyerId = buyerId;
        this.soldAt = LocalDateTime.now();
    }

    public void markExpired() {
        this.expired = true;
        this.expiredAt = LocalDateTime.now();
    }

    /**
     * Kiểm tra xem item có hết hạn không (dựa trên expiresAt)
     */
    public boolean isExpired() {
        if (this.expiresAt == null) {
            return false; // Không có thời gian hết hạn
        }
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
