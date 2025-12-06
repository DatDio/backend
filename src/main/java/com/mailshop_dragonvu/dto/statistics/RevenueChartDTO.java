package com.mailshop_dragonvu.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho dữ liệu biểu đồ doanh thu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartDTO {

    private List<String> labels;      // Các nhãn trục X (ngày/tháng)
    private List<Long> revenues;      // Doanh thu tương ứng
    private List<Long> orderCounts;   // Số đơn hàng tương ứng

    private Long totalRevenue;        // Tổng doanh thu trong khoảng
    private Long totalOrders;         // Tổng đơn hàng trong khoảng
    private Double avgOrderValue;     // Giá trị trung bình mỗi đơn
}
