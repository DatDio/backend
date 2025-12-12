package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.ranks.*;
import com.mailshop_dragonvu.service.RankService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController("adminRankController")
@RequestMapping("/admin/" + Constants.API_PATH.RANKS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Quản lý thứ hạng", description = "CRUD thứ hạng (Admin)")
@SecurityRequirement(name = "Bearer Authentication")
public class RankController {

    private final RankService rankService;

    @GetMapping
    @Operation(summary = "Danh sách thứ hạng", description = "Lấy danh sách thứ hạng có phân trang và lọc")
    public ApiResponse<Page<RankResponseDTO>> searchRanks(RankFilterDTO filter) {
        log.info("Searching ranks with filter: {}", filter);
        Page<RankResponseDTO> result = rankService.searchRanks(filter);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết thứ hạng", description = "Lấy thông tin thứ hạng theo ID")
    public ApiResponse<RankResponseDTO> getRankById(@PathVariable Long id) {
        log.info("Getting rank by ID: {}", id);
        RankResponseDTO result = rankService.getRankById(id);
        return ApiResponse.success(result);
    }

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo thứ hạng", description = "Tạo thứ hạng mới")
    public ApiResponse<RankResponseDTO> createRank(@Valid @ModelAttribute RankCreateDTO request) {
        log.info("Creating new rank: {}", request.getName());
        RankResponseDTO result = rankService.createRank(request);
        return ApiResponse.success("Tạo thứ hạng thành công", result);
    }

    @PutMapping(value = "/update/{id}", consumes = "multipart/form-data")
    @Operation(summary = "Cập nhật thứ hạng", description = "Cập nhật thông tin thứ hạng")
    public ApiResponse<RankResponseDTO> updateRank(
            @PathVariable Long id,
            @Valid @ModelAttribute RankUpdateDTO request) {
        log.info("Updating rank ID: {}", id);
        RankResponseDTO result = rankService.updateRank(id, request);
        return ApiResponse.success("Cập nhật thứ hạng thành công", result);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Xóa thứ hạng", description = "Xóa thứ hạng")
    public ApiResponse<Void> deleteRank(@PathVariable Long id) {
        log.info("Deleting rank ID: {}", id);
        rankService.deleteRank(id);
        return ApiResponse.success("Xóa thứ hạng thành công");
    }
}
