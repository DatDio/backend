package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.statistics.DashboardOverviewDTO;
import com.mailshop_dragonvu.dto.statistics.RevenueChartDTO;
import com.mailshop_dragonvu.dto.statistics.StatisticsFilterDTO;
import com.mailshop_dragonvu.entity.DailyStatisticsEntity;
import com.mailshop_dragonvu.repository.DailyStatisticsRepository;
import com.mailshop_dragonvu.repository.OrderRepository;
import com.mailshop_dragonvu.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderRepository orderRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;

    @Override
    public DashboardOverviewDTO getOverviewStats() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        // ========== Thống kê hôm nay (từ OrderRepository - chưa bị xóa) ==========
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        
        Long ordersToday = orderRepository.countOrdersByDateRange(todayStart, todayEnd);
        Long revenueToday = orderRepository.sumRevenueByDateRange(todayStart, todayEnd);
        if (revenueToday == null) revenueToday = 0L;

        // ========== Thống kê hôm qua (từ DailyStatistics nếu có, fallback OrderRepository) ==========
        Long ordersYesterday;
        Long revenueYesterday;
        
        DailyStatisticsEntity yesterdayStats = dailyStatisticsRepository.findByStatDate(yesterday).orElse(null);
        if (yesterdayStats != null) {
            ordersYesterday = yesterdayStats.getTotalOrders();
            revenueYesterday = yesterdayStats.getTotalRevenue();
        } else {
            // Fallback: lấy từ OrderRepository nếu chưa có trong DailyStatistics
            LocalDateTime yesterdayStart = yesterday.atStartOfDay();
            LocalDateTime yesterdayEnd = yesterday.atTime(LocalTime.MAX);
            ordersYesterday = orderRepository.countOrdersByDateRange(yesterdayStart, yesterdayEnd);
            revenueYesterday = orderRepository.sumRevenueByDateRange(yesterdayStart, yesterdayEnd);
        }
        if (revenueYesterday == null) revenueYesterday = 0L;

        // ========== Tính tăng trưởng ==========
        Double ordersGrowth = calculateGrowthPercent(ordersToday, ordersYesterday);
        Double revenueGrowth = calculateGrowthPercent(revenueToday, revenueYesterday);

        // ========== Thống kê tháng này (Kết hợp DailyStatistics + hôm nay) ==========
        Long ordersThisMonth;
        Long revenueThisMonth;
        
        if (today.getDayOfMonth() == 1) {
            // Ngày 1 của tháng: chỉ có dữ liệu hôm nay
            ordersThisMonth = ordersToday;
            revenueThisMonth = revenueToday;
        } else {
            // Các ngày khác: lấy từ DailyStatistics (ngày 1 -> hôm qua) + hôm nay
            ordersThisMonth = dailyStatisticsRepository.sumOrdersByDateRange(firstDayOfMonth, yesterday);
            revenueThisMonth = dailyStatisticsRepository.sumRevenueByDateRange(firstDayOfMonth, yesterday);
            ordersThisMonth = (ordersThisMonth != null ? ordersThisMonth : 0L) + ordersToday;
            revenueThisMonth = (revenueThisMonth != null ? revenueThisMonth : 0L) + revenueToday;
        }

        // ========== Thống kê tổng (từ DailyStatistics + hôm nay) ==========
        Long totalOrders = dailyStatisticsRepository.sumTotalOrders();
        Long totalRevenue = dailyStatisticsRepository.sumTotalRevenue();
        totalOrders = (totalOrders != null ? totalOrders : 0L) + ordersToday;
        totalRevenue = (totalRevenue != null ? totalRevenue : 0L) + revenueToday;

        return DashboardOverviewDTO.builder()
                .ordersToday(ordersToday)
                .ordersYesterday(ordersYesterday)
                .ordersGrowthPercent(ordersGrowth)
                .revenueToday(revenueToday)
                .revenueYesterday(revenueYesterday)
                .revenueGrowthPercent(revenueGrowth)
                .ordersThisMonth(ordersThisMonth)
                .revenueThisMonth(revenueThisMonth)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    public RevenueChartDTO getRevenueChartData(StatisticsFilterDTO filter) {
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        LocalDate startDate = filter.getStartDate();

        // Nếu có period, tính startDate từ period
        if (filter.getPeriod() != null && startDate == null) {
            startDate = switch (filter.getPeriod().toUpperCase()) {
                case "WEEK" -> endDate.minusDays(6);
                case "MONTH" -> endDate.minusDays(29);
                case "YEAR" -> endDate.minusMonths(11).withDayOfMonth(1);
                default -> endDate.minusDays(6);
            };
        }

        if (startDate == null) {
            startDate = endDate.minusDays(6);
        }

        List<String> labels = new ArrayList<>();
        List<Long> revenues = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        boolean isYearlyView = "YEAR".equalsIgnoreCase(filter.getPeriod());

        // Lấy dữ liệu từ DailyStatistics cho các ngày trong quá khứ
        List<DailyStatisticsEntity> dailyStats = dailyStatisticsRepository.findByDateRange(startDate, endDate);
        Map<LocalDate, DailyStatisticsEntity> statsMap = dailyStats.stream()
                .collect(Collectors.toMap(DailyStatisticsEntity::getStatDate, s -> s));

        LocalDate today = LocalDate.now();

        if (isYearlyView) {
            // Thống kê theo tháng (12 tháng)
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
            for (int i = 11; i >= 0; i--) {
                LocalDate monthStart = endDate.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                if (monthEnd.isAfter(endDate)) {
                    monthEnd = endDate;
                }

                // Tính tổng cho tháng từ DailyStatistics
                Long monthRevenue = dailyStatisticsRepository.sumRevenueByDateRange(monthStart, monthEnd);
                Long monthOrders = dailyStatisticsRepository.sumOrdersByDateRange(monthStart, monthEnd);

                // Nếu tháng này có ngày hôm nay, cộng thêm dữ liệu hôm nay từ OrderRepository
                if (!today.isBefore(monthStart) && !today.isAfter(monthEnd)) {
                    LocalDateTime todayStart = today.atStartOfDay();
                    LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
                    Long todayRevenue = orderRepository.sumRevenueByDateRange(todayStart, todayEnd);
                    Long todayOrders = orderRepository.countOrdersByDateRange(todayStart, todayEnd);
                    monthRevenue = (monthRevenue != null ? monthRevenue : 0L) + (todayRevenue != null ? todayRevenue : 0L);
                    monthOrders = (monthOrders != null ? monthOrders : 0L) + (todayOrders != null ? todayOrders : 0L);
                }

                labels.add(monthStart.format(monthFormatter));
                revenues.add(monthRevenue != null ? monthRevenue : 0L);
                orderCounts.add(monthOrders != null ? monthOrders : 0L);
            }
        } else {
            // Thống kê theo ngày
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                Long revenue;
                Long orders;

                if (current.equals(today)) {
                    // Ngày hôm nay: lấy từ OrderRepository
                    LocalDateTime dayStart = current.atStartOfDay();
                    LocalDateTime dayEnd = current.atTime(LocalTime.MAX);
                    revenue = orderRepository.sumRevenueByDateRange(dayStart, dayEnd);
                    orders = orderRepository.countOrdersByDateRange(dayStart, dayEnd);
                } else {
                    // Ngày trong quá khứ: lấy từ DailyStatistics
                    DailyStatisticsEntity stat = statsMap.get(current);
                    if (stat != null) {
                        revenue = stat.getTotalRevenue();
                        orders = stat.getTotalOrders();
                    } else {
                        // Fallback nếu chưa có DailyStatistics (order chưa bị xóa)
                        LocalDateTime dayStart = current.atStartOfDay();
                        LocalDateTime dayEnd = current.atTime(LocalTime.MAX);
                        revenue = orderRepository.sumRevenueByDateRange(dayStart, dayEnd);
                        orders = orderRepository.countOrdersByDateRange(dayStart, dayEnd);
                    }
                }

                labels.add(current.format(formatter));
                revenues.add(revenue != null ? revenue : 0L);
                orderCounts.add(orders != null ? orders : 0L);

                current = current.plusDays(1);
            }
        }

        // Tính tổng
        Long totalRevenue = revenues.stream().mapToLong(Long::longValue).sum();
        Long totalOrders = orderCounts.stream().mapToLong(Long::longValue).sum();
        Double avgOrderValue = totalOrders > 0 ? (double) totalRevenue / totalOrders : 0.0;

        return RevenueChartDTO.builder()
                .labels(labels)
                .revenues(revenues)
                .orderCounts(orderCounts)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .avgOrderValue(avgOrderValue)
                .build();
    }

    /**
     * Tính % tăng trưởng
     */
    private Double calculateGrowthPercent(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }
}
