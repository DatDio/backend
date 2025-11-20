package com.mailshop_dragonvu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mailshop_dragonvu.enums.OrderStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_order_number", columnList = "order_number"),
        @Index(name = "idx_orders_status", columnList = "order_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderEntity extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_user"))
    @JsonIgnore
    private UserEntity user;

    @Column(name = "order_status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatusEnum orderStatus = com.mailshop_dragonvu.enums.OrderStatusEnum.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    // ---------------- Business Logic ---------------- //

    /** Add item to order & auto set relation  */
    public void addOrderItem(OrderItemEntity item) {
        if (!orderItems.contains(item)) {
            orderItems.add(item);
            item.setOrder(this);
        }
    }

    /** Remove item */
    public void removeOrderItem(OrderItemEntity item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    /** Recalculate final amount based on total - discount */
    public void calculateFinalAmount() {
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        this.finalAmount = totalAmount.subtract(discountAmount);
    }

    /** Mark order as completed */
    public void markAsCompleted() {
        this.orderStatus = com.mailshop_dragonvu.enums.OrderStatusEnum.COMPLETED;
        this.completedDate = LocalDateTime.now();
    }

    /** Mark order as cancelled with reason */
    public void markAsCancelled(String reason) {
        this.orderStatus = com.mailshop_dragonvu.enums.OrderStatusEnum.CANCELLED;
        this.cancelledDate = LocalDateTime.now();
        this.cancellationReason = reason;
    }
}
