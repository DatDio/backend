package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.ranks.*;
import com.mailshop_dragonvu.entity.RankEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.mapper.RankMapper;
import com.mailshop_dragonvu.repository.RankRepository;
import com.mailshop_dragonvu.repository.TransactionRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.service.FileUploadService;
import com.mailshop_dragonvu.service.RankService;
import com.mailshop_dragonvu.service.SystemSettingService;
import com.mailshop_dragonvu.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RankServiceImpl implements RankService {

    private final RankRepository rankRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RankMapper rankMapper;
    private final SystemSettingService systemSettingService;
    private final FileUploadService fileUploadService;

    private static final String ICON_FOLDER = "icons";

    private static final String RANK_NOT_FOUND = "Không tìm thấy thứ hạng";
    private static final String RANK_NAME_EXISTS = "Tên thứ hạng đã tồn tại";
    private static final String SETTING_RANK_PERIOD_DAYS = "rank.period_days";
    private static final Integer DEFAULT_PERIOD_DAYS = 7;

    @Override
    public RankResponseDTO createRank(RankCreateDTO request) {
        log.info("Creating new rank: {}", request.getName());

        // Check if rank name already exists
        if (rankRepository.existsByName(request.getName().trim())) {
            throw new BusinessException(RANK_NAME_EXISTS);
        }

        RankEntity entity = rankMapper.toEntity(request);

        // Handle icon upload
        if (request.getIcon() != null && !request.getIcon().isEmpty()) {
            String iconUrl = fileUploadService.uploadImage(request.getIcon(), ICON_FOLDER);
            entity.setIconUrl(iconUrl);
        }

        entity = rankRepository.save(entity);

        log.info("Rank created successfully with ID: {}", entity.getId());
        return rankMapper.toResponse(entity);
    }

    @Override
    public RankResponseDTO updateRank(Long id, RankUpdateDTO request) {
        log.info("Updating rank with ID: {}", id);

        RankEntity entity = findRankOrThrow(id);

        // Check name uniqueness if name is being changed
        if (request.getName() != null && !request.getName().trim().equalsIgnoreCase(entity.getName())) {
            if (rankRepository.existsByName(request.getName().trim())) {
                throw new BusinessException(RANK_NAME_EXISTS);
            }
        }

        rankMapper.updateEntity(entity, request);

        if (entity.getIconUrl() != null) {
            fileUploadService.deleteFile(entity.getIconUrl());
        }

        // Handle icon upload
        if (request.getIcon() != null && !request.getIcon().isEmpty()) {
            String iconUrl = fileUploadService.uploadImage(request.getIcon(), ICON_FOLDER);
            entity.setIconUrl(iconUrl);
        }

        entity = rankRepository.save(entity);

        log.info("Rank updated successfully with ID: {}", id);
        return rankMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public RankResponseDTO getRankById(Long id) {
        log.debug("Getting rank by ID: {}", id);
        RankEntity entity = findRankOrThrow(id);
        return rankMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RankResponseDTO> searchRanks(RankFilterDTO request) {
        Sort sort = Utils.generatedSort(request.getSort());

        Pageable pageable;
        if (request.getLimit() == null) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);
        } else {
            int page = Optional.ofNullable(request.getPage()).orElse(0);
            pageable = PageRequest.of(page, request.getLimit(), sort);
        }

        Specification<RankEntity> spec = buildSearchSpecification(request);
        Page<RankEntity> ranks = rankRepository.findAll(spec, pageable);

        return ranks.map(rankMapper::toResponse);
    }

    @Override
    public void deleteRank(Long id) {
        log.info("Deleting rank with ID: {}", id);
        RankEntity entity = findRankOrThrow(id);
        rankRepository.delete(entity);
        log.info("Rank deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankResponseDTO> getAllActiveRanks() {
        List<RankEntity> ranks = rankRepository.findAllActiveOrderByMinDeposit(ActiveStatusEnum.ACTIVE);
        return ranks.stream()
                .map(rankMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserRankInfoDTO getUserRankInfo(Long userId) {
        log.debug("Getting rank info for user ID: {}", userId);

        // Get all active ranks to find current and next rank
        List<RankEntity> allRanks = rankRepository.findAllActiveOrderByMinDeposit(ActiveStatusEnum.ACTIVE);

        // Get period days from system settings
        Integer periodDays = systemSettingService.getIntValue(SETTING_RANK_PERIOD_DAYS, DEFAULT_PERIOD_DAYS);
        Long totalDeposit = getTotalDepositInPeriod(userId, periodDays);

        if (allRanks.isEmpty()) {
            // Get CTV info from user even if no ranks
            UserEntity user = userRepository.findById(userId).orElse(null);
            Boolean isCollaborator = user != null && Boolean.TRUE.equals(user.getIsCollaborator());
            Integer ctvBonusPercent = (user != null && isCollaborator) ? user.getBonusPercent() : 0;
            
            return UserRankInfoDTO.builder()
                    .rankName("Không có")
                    .bonusPercent(0)
                    .currentDeposit(totalDeposit)
                    .periodDays(periodDays)
                    .isCollaborator(isCollaborator)
                    .ctvBonusPercent(ctvBonusPercent != null ? ctvBonusPercent : 0)
                    .build();
        }



        // Find current rank (highest minDeposit that is <= totalDeposit)
        RankEntity currentRank = null;
        RankEntity nextRank = null;

        for (int i = 0; i < allRanks.size(); i++) {
            RankEntity rank = allRanks.get(i);
            if (rank.getMinDeposit() <= totalDeposit) {
                currentRank = rank;
                // Next rank is the one after current (if exists)
                if (i + 1 < allRanks.size()) {
                    nextRank = allRanks.get(i + 1);
                }
            }
        }

        // If no rank found, use the first (lowest) rank
        if (currentRank == null && !allRanks.isEmpty()) {
            currentRank = allRanks.get(0);
            if (allRanks.size() > 1) {
                nextRank = allRanks.get(1);
            }
        }

        // Get CTV info from user
        UserEntity user = userRepository.findById(userId).orElse(null);
        Boolean isCollaborator = user != null && Boolean.TRUE.equals(user.getIsCollaborator());
        Integer ctvBonusPercent = (user != null && isCollaborator) ? user.getBonusPercent() : 0;

        return UserRankInfoDTO.builder()
                .rankId(currentRank != null ? currentRank.getId() : null)
                .rankName(currentRank != null ? currentRank.getName() : "Không có")
                .bonusPercent(currentRank != null ? currentRank.getBonusPercent() : 0)
                .iconUrl(currentRank != null ? currentRank.getIconUrl() : null)
                .color(currentRank != null ? currentRank.getColor() : null)
                .currentDeposit(totalDeposit)
                .nextRankMinDeposit(nextRank != null ? nextRank.getMinDeposit() : null)
                .nextRankName(nextRank != null ? nextRank.getName() : null)
                .periodDays(periodDays)
                .isCollaborator(isCollaborator)
                .ctvBonusPercent(ctvBonusPercent != null ? ctvBonusPercent : 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long calculateDepositBonus(Long userId, Long depositAmount) {
        UserRankInfoDTO rankInfo = getUserRankInfo(userId);
        int rankBonus = rankInfo.getBonusPercent();
        
        // Get collaborator bonus from user
        int ctvBonus = 0;
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user != null && Boolean.TRUE.equals(user.getIsCollaborator())) {
            ctvBonus = user.getBonusPercent() != null ? user.getBonusPercent() : 0;
            log.info("CTV bonus for user {}: {}%", userId, ctvBonus);
        }
        
        // Total bonus = Rank bonus + CTV bonus
        int totalBonusPercent = rankBonus + ctvBonus;
        
        if (totalBonusPercent <= 0) {
            return 0L;
        }

        // Calculate bonus: depositAmount * totalBonusPercent / 100
        Long bonus = (depositAmount * totalBonusPercent) / 100;
        log.info("Deposit bonus for user {}: {}% (rank: {}% + ctv: {}%) = {} VND", 
                userId, totalBonusPercent, rankBonus, ctvBonus, bonus);
        return bonus;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalDepositInPeriod(Long userId, Integer periodDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(periodDays);
        Long total = transactionRepository.getTotalDepositInPeriod(userId, since);
        return total != null ? total : 0L;
    }

    private RankEntity findRankOrThrow(Long id) {
        return rankRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RANK_NOT_FOUND));
    }

    private Specification<RankEntity> buildSearchSpecification(RankFilterDTO request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")), 
                        "%" + request.getName().toLowerCase().trim() + "%"));
            }

            if (StringUtils.hasText(request.getStatus())) {
                try {
                    ActiveStatusEnum status = ActiveStatusEnum.fromKey(Integer.parseInt(request.getStatus()));
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (NumberFormatException ignored) {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
