package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.ranks.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RankService {

    // CRUD Operations
    RankResponseDTO createRank(RankCreateDTO request);
    
    RankResponseDTO updateRank(Long id, RankUpdateDTO request);
    
    RankResponseDTO getRankById(Long id);
    
    Page<RankResponseDTO> searchRanks(RankFilterDTO request);
    
    void deleteRank(Long id);

    // Get all active ranks (for client display)
    List<RankResponseDTO> getAllActiveRanks();

    // Get user's current rank info
    UserRankInfoDTO getUserRankInfo(Long userId);

    // Calculate deposit bonus based on user's rank
    Long calculateDepositBonus(Long userId, Long depositAmount);

    // Get total deposit in period for user
    Long getTotalDepositInPeriod(Long userId, Integer periodDays);
}
