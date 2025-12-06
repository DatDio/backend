package com.mailshop_dragonvu.scheduler;

import com.mailshop_dragonvu.entity.DailyStatisticsEntity;
import com.mailshop_dragonvu.repository.DailyStatisticsRepository;
import com.mailshop_dragonvu.repository.OrderRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Scheduler tổng hợp thống kê hàng ngày
 * Chạy lúc 23:55 mỗi ngày để lưu dữ liệu trước khi qua ngày mới
 * Dữ liệu này sẽ được giữ lại vĩnh viễn dù order bị xóa
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyStatisticsScheduler {

    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * Tổng hợp thống kê cuối ngày - chạy lúc 23:55 mỗi ngày
     */
    @Scheduled(cron = "${app.statistics.cron:0 55 23 * * ?}")
    @Transactional
    public void aggregateDailyStatistics() {
        LocalDate today = LocalDate.now();
        log.info("Starting daily statistics aggregation for: {}", today);

        try {
            aggregateForDate(today);
            log.info("Daily statistics aggregation completed for: {}", today);
        } catch (Exception e) {
            log.error("Error during daily statistics aggregation: {}", e.getMessage(), e);
        }
    }

    /**
     * Tổng hợp thống kê cho một ngày cụ thể
     * Public để có thể gọi thủ công khi cần backfill data
     */
    @Transactional
    public void aggregateForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Lấy hoặc tạo mới record thống kê cho ngày
        DailyStatisticsEntity stats = dailyStatisticsRepository.findByStatDate(date)
                .orElse(DailyStatisticsEntity.builder()
                        .statDate(date)
                        .totalOrders(0L)
                        .totalRevenue(0L)
                        .newUsers(0L)
                        .totalItemsSold(0L)
                        .build());

        // Lấy dữ liệu từ order (COMPLETED orders)
        Long ordersCount = orderRepository.countOrdersByDateRange(startOfDay, endOfDay);
        Long revenue = orderRepository.sumRevenueByDateRange(startOfDay, endOfDay);

        // Lấy số user mới đăng ký
        Long newUsers = userRepository.countNewUsersByDateRange(startOfDay, endOfDay);

        // Cập nhật thống kê
        stats.setTotalOrders(ordersCount != null ? ordersCount : 0L);
        stats.setTotalRevenue(revenue != null ? revenue : 0L);
        stats.setNewUsers(newUsers != null ? newUsers : 0L);

        dailyStatisticsRepository.save(stats);

        log.debug("Saved statistics for {}: orders={}, revenue={}, newUsers={}",
                date, stats.getTotalOrders(), stats.getTotalRevenue(), stats.getNewUsers());
    }

    /**
     * Backfill data cho các ngày trước đó (chạy thủ công khi cần)
     * Gọi API hoặc chạy từ console
     */
    @Transactional
    public void backfillStatistics(LocalDate fromDate, LocalDate toDate) {
        log.info("Starting backfill from {} to {}", fromDate, toDate);

        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            try {
                aggregateForDate(current);
            } catch (Exception e) {
                log.error("Error backfilling stats for {}: {}", current, e.getMessage());
            }
            current = current.plusDays(1);
        }

        log.info("Backfill completed from {} to {}", fromDate, toDate);
    }
}
