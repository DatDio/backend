package com.mailshop_dragonvu.dto.ranks;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankResponseDTO {

    private Long id;
    private String name;
    private Integer bonusPercent;
    private Long minDeposit;
    private String iconUrl;
    private String color;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
