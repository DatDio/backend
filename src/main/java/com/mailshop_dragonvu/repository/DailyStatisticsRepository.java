package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.DailyStatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatisticsRepository extends JpaRepository<DailyStatisticsEntity, Long> {

    /**
     * Tìm thống kê theo ngày
     */
    Optional<DailyStatisticsEntity> findByStatDate(LocalDate statDate);

    /**
     * Lấy thống kê trong khoảng thời gian
     */
    @Query("SELECT d FROM DailyStatisticsEntity d WHERE d.statDate BETWEEN :startDate AND :endDate ORDER BY d.statDate ASC")
    List<DailyStatisticsEntity> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng doanh thu trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(d.totalRevenue), 0) FROM DailyStatisticsEntity d WHERE d.statDate BETWEEN :startDate AND :endDate")
    Long sumRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng đơn hàng trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(d.totalOrders), 0) FROM DailyStatisticsEntity d WHERE d.statDate BETWEEN :startDate AND :endDate")
    Long sumOrdersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng doanh thu tất cả thời gian
     */
    @Query("SELECT COALESCE(SUM(d.totalRevenue), 0) FROM DailyStatisticsEntity d")
    Long sumTotalRevenue();

    /**
     * Tính tổng đơn hàng tất cả thời gian
     */
    @Query("SELECT COALESCE(SUM(d.totalOrders), 0) FROM DailyStatisticsEntity d")
    Long sumTotalOrders();
}
