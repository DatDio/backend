package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "base_seq_gen", sequenceName = "ORDER_SEQ", allocationSize = 1)
public class Order extends BaseEntity {

    @Column(name = "ORDER_NUMBER", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "DISCOUNT_AMOUNT", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "FINAL_AMOUNT", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "NOTES", length = 1000)
    private String notes;

    @Column(name = "COMPLETED_DATE")
    private LocalDateTime completedDate;

    @Column(name = "CANCELLED_DATE")
    private LocalDateTime cancelledDate;

    @Column(name = "CANCELLATION_REASON", length = 500)
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    public void calculateFinalAmount() {
        this.finalAmount = this.totalAmount.subtract(this.discountAmount);
    }

    /**
     * Mark order as completed (digital product delivered instantly)
     */
    public void markAsCompleted() {
        this.orderStatus = OrderStatus.COMPLETED;
        this.completedDate = LocalDateTime.now();
    }

    /**
     * Mark order as cancelled
     */
    public void markAsCancelled(String reason) {
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelledDate = LocalDateTime.now();
        this.cancellationReason = reason;
    }

}
