package com.mailshop_dragonvu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ORDER_ITEMS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "base_seq_gen", sequenceName = "ORDER_ITEM_SEQ", allocationSize = 1)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    @Column(name = "PRODUCT_NAME", nullable = false, length = 255)
    private String productName;

    @Column(name = "PRODUCT_SKU", length = 100)
    private String productSku;

    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    @Column(name = "UNIT_PRICE", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "DISCOUNT_AMOUNT", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "TAX_AMOUNT", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "TOTAL_PRICE", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "NOTES", length = 500)
    private String notes;

    public void calculateTotalPrice() {
        BigDecimal subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        this.totalPrice = subtotal.subtract(this.discountAmount).add(this.taxAmount);
    }

}
