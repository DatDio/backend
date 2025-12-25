package com.mailshop_dragonvu.entity;

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

    // Trạng thái hết hạn (mail quá thời gian sống)
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
     * Kiểm tra xem item có hết hạn không
     * @param expirationHours Số giờ hết hạn (0 = không hết hạn)
     */
    public boolean isExpired(int expirationHours) {
        if (expirationHours <= 0) {
            return false; // Không có thời gian hết hạn
        }
        if (this.getCreatedAt() == null) {
            return false;
        }
        LocalDateTime expiryTime = this.getCreatedAt().plusHours(expirationHours);
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
