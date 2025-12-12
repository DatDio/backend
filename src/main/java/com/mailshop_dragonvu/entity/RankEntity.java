package com.mailshop_dragonvu.entity;

import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Rank Entity - User ranking/tier system
 * Users are assigned ranks based on total deposit amount within a period
 */
@Entity
@Table(name = "ranks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RankEntity extends BaseEntity {

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "bonus_percent", nullable = false)
    @Builder.Default
    private Integer bonusPercent = 0;

    @Column(name = "min_deposit", nullable = false)
    @Builder.Default
    private Long minDeposit = 0L;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "color", length = 20)
    private String color;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private ActiveStatusEnum status = ActiveStatusEnum.ACTIVE;
}
