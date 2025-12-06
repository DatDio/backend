package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.statistics.DashboardOverviewDTO;
import com.mailshop_dragonvu.dto.statistics.RevenueChartDTO;
import com.mailshop_dragonvu.dto.statistics.StatisticsFilterDTO;

/**
 * Service interface cho thống kê Dashboard
 */
public interface StatisticsService {

    /**
     * Lấy thống kê tổng quan cho Dashboard
     * Bao gồm: đơn hàng hôm nay, doanh thu hôm nay, tăng trưởng...
     */
    DashboardOverviewDTO getOverviewStats();

    /**
     * Lấy dữ liệu biểu đồ doanh thu theo khoảng thời gian
     * @param filter Bộ lọc thời gian
     * @return Dữ liệu cho Chart.js
     */
    RevenueChartDTO getRevenueChartData(StatisticsFilterDTO filter);
}
