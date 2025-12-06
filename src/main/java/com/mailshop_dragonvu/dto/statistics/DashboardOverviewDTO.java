package com.mailshop_dragonvu.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho thống kê tổng quan Dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDTO {

    // ========== Đơn hàng hôm nay ==========
    private Long ordersToday;
    private Long ordersYesterday;
    private Double ordersGrowthPercent;  // % tăng trưởng so với hôm qua

    // ========== Doanh thu hôm nay ==========
    private Long revenueToday;
    private Long revenueYesterday;
    private Double revenueGrowthPercent;  // % tăng trưởng so với hôm qua

    // ========== Thống kê tháng này ==========
    private Long ordersThisMonth;
    private Long revenueThisMonth;

    // ========== Thống kê tổng ==========
    private Long totalOrders;
    private Long totalRevenue;
}
