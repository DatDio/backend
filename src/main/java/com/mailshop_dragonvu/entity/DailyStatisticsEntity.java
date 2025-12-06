package com.mailshop_dragonvu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Entity lưu trữ thống kê hàng ngày
 * Dữ liệu này sẽ được giữ lại vĩnh viễn, không bị ảnh hưởng khi xóa order
 */
@Entity
@Table(name = "daily_statistics", indexes = {
        @Index(name = "idx_daily_stats_date", columnList = "stat_date", unique = true)
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatisticsEntity extends BaseEntity {

    @Column(name = "stat_date", nullable = false, unique = true)
    private LocalDate statDate;

    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Long totalOrders = 0L;

    @Column(name = "total_revenue", nullable = false)
    @Builder.Default
    private Long totalRevenue = 0L;

    @Column(name = "new_users", nullable = false)
    @Builder.Default
    private Long newUsers = 0L;

    @Column(name = "total_items_sold", nullable = false)
    @Builder.Default
    private Long totalItemsSold = 0L;

    /**
     * Cộng thêm dữ liệu vào thống kê
     */
    public void addStats(Long orders, Long revenue, Long itemsSold) {
        this.totalOrders += orders;
        this.totalRevenue += revenue;
        this.totalItemsSold += itemsSold;
    }
}
