package com.mailshop_dragonvu.dto.ranks;

import lombok.*;

/**
 * DTO to hold user's current rank information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRankInfoDTO {

    private Long rankId;
    private String rankName;
    private Integer bonusPercent;
    private String iconUrl;
    private String color;
    private Long currentDeposit; // Total deposit in current period
    private Long nextRankMinDeposit; // Min deposit for next rank (null if at highest)
    private String nextRankName; // Name of next rank (null if at highest)
    private Integer periodDays;
}
