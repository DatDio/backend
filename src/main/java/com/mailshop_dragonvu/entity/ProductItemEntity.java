package com.mailshop_dragonvu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_items", indexes = {
        @Index(name = "idx_product_items_product_id", columnList = "product_id"),
        @Index(name = "idx_product_items_sold", columnList = "sold")
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

    @Column(name = "buyer_id")
    private Long buyerId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    public void markSold(Long buyerId, Long orderId) {
        this.sold = true;
        this.buyerId = buyerId;
        this.orderId = orderId;
        this.soldAt = LocalDateTime.now();
    }
}

