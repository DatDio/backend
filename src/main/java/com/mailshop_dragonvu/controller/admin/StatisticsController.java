package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.statistics.DashboardOverviewDTO;
import com.mailshop_dragonvu.dto.statistics.RevenueChartDTO;
import com.mailshop_dragonvu.dto.statistics.StatisticsFilterDTO;
import com.mailshop_dragonvu.scheduler.DailyStatisticsScheduler;
import com.mailshop_dragonvu.service.StatisticsService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/" + Constants.API_PATH.STATISTICS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Statistics", description = "APIs thống kê cho Admin Dashboard")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final DailyStatisticsScheduler dailyStatisticsScheduler;

    @GetMapping("/overview")
    @Operation(summary = "Lấy thống kê tổng quan Dashboard")
    public ApiResponse<DashboardOverviewDTO> getOverviewStats() {
        return ApiResponse.success(statisticsService.getOverviewStats());
    }

    @GetMapping("/revenue-chart")
    @Operation(summary = "Lấy dữ liệu biểu đồ doanh thu theo thời gian")
    public ApiResponse<RevenueChartDTO> getRevenueChartData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "WEEK") String period) {

        StatisticsFilterDTO filter = StatisticsFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .period(period)
                .build();

        return ApiResponse.success(statisticsService.getRevenueChartData(filter));
    }

    @PostMapping("/backfill")
    @Operation(summary = "Backfill thống kê cho các ngày trong quá khứ (chạy 1 lần khi cần)")
    public ApiResponse<String> backfillStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        dailyStatisticsScheduler.backfillStatistics(fromDate, toDate);
        return ApiResponse.success("Backfill completed from " + fromDate + " to " + toDate);
    }

    @PostMapping("/aggregate-today")
    @Operation(summary = "Tổng hợp thống kê cho ngày hôm nay (test)")
    public ApiResponse<String> aggregateToday() {
        dailyStatisticsScheduler.aggregateForDate(LocalDate.now());
        return ApiResponse.success("Aggregated statistics for today");
    }
}

