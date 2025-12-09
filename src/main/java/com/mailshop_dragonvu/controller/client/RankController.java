package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.ranks.RankResponseDTO;
import com.mailshop_dragonvu.dto.ranks.UserRankInfoDTO;
import com.mailshop_dragonvu.security.UserPrincipal;
import com.mailshop_dragonvu.service.RankService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("clientRankController")
@RequestMapping(Constants.API_PATH.RANKS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Thứ hạng người dùng", description = "API thứ hạng cho client")
@SecurityRequirement(name = "Bearer Authentication")
public class RankController {

    private final RankService rankService;

    @GetMapping
    @Operation(summary = "Danh sách thứ hạng", description = "Lấy tất cả thứ hạng đang hoạt động")
    public ApiResponse<List<RankResponseDTO>> getAllRanks() {
        log.debug("Getting all active ranks");
        List<RankResponseDTO> ranks = rankService.getAllActiveRanks();
        return ApiResponse.success(ranks);
    }

    @GetMapping("/my-rank")
    @Operation(summary = "Thứ hạng của tôi", description = "Lấy thông tin thứ hạng hiện tại của user")
    public ApiResponse<UserRankInfoDTO> getMyRank(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.debug("Getting rank for user ID: {}", userPrincipal.getId());
        UserRankInfoDTO rankInfo = rankService.getUserRankInfo(userPrincipal.getId());
        return ApiResponse.success(rankInfo);
    }
}
