package com.mailshop_dragonvu.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho filter thống kê theo thời gian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsFilterDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    
    /**
     * Loại filter nhanh: TODAY, WEEK, MONTH, YEAR
     */
    private String period;
}
