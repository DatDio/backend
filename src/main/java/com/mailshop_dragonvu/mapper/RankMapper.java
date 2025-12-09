package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.ranks.RankCreateDTO;
import com.mailshop_dragonvu.dto.ranks.RankResponseDTO;
import com.mailshop_dragonvu.dto.ranks.RankUpdateDTO;
import com.mailshop_dragonvu.entity.RankEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.utils.EnumParseUtils;
import org.springframework.stereotype.Component;

@Component
public class RankMapper {

    public RankResponseDTO toResponse(RankEntity entity) {
        if (entity == null) return null;

        return RankResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .bonusPercent(entity.getBonusPercent())
                .minDeposit(entity.getMinDeposit())
                .periodDays(entity.getPeriodDays())
                .displayOrder(entity.getDisplayOrder())
                .iconUrl(entity.getIconUrl())
                .color(entity.getColor())
                .status(entity.getStatus().getKey())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public RankEntity toEntity(RankCreateDTO request) {
        if (request == null) return null;

        return RankEntity.builder()
                .name(request.getName())
                .bonusPercent(request.getBonusPercent() != null ? request.getBonusPercent() : 0)
                .minDeposit(request.getMinDeposit() != null ? request.getMinDeposit() : 0L)
                .periodDays(request.getPeriodDays() != null ? request.getPeriodDays() : 7)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .iconUrl(request.getIconUrl())
                .color(request.getColor())
                .status(ActiveStatusEnum.ACTIVE)
                .build();
    }

    public void updateEntity(RankEntity entity, RankUpdateDTO request) {
        if (entity == null || request == null) return;

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getBonusPercent() != null) {
            entity.setBonusPercent(request.getBonusPercent());
        }
        if (request.getMinDeposit() != null) {
            entity.setMinDeposit(request.getMinDeposit());
        }
        if (request.getPeriodDays() != null) {
            entity.setPeriodDays(request.getPeriodDays());
        }
        if (request.getDisplayOrder() != null) {
            entity.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIconUrl() != null) {
            entity.setIconUrl(request.getIconUrl());
        }
        if (request.getColor() != null) {
            entity.setColor(request.getColor());
        }
        if (request.getStatus() != null) {
            entity.setStatus(ActiveStatusEnum.fromKey(request.getStatus()));
        }
    }
}
