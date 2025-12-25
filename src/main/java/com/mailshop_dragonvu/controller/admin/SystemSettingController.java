package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.settings.SystemSettingDTO;
import com.mailshop_dragonvu.dto.settings.SystemSettingUpdateDTO;
import com.mailshop_dragonvu.service.SystemSettingService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSystemSettingController")
@RequestMapping("/admin/" + Constants.API_PATH.SETTINGS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    @Operation(summary = "Danh sách cài đặt", description = "Lấy tất cả cài đặt hệ thống")
    public ApiResponse<List<SystemSettingDTO>> getAllSettings() {
        log.info("Getting all system settings");
        List<SystemSettingDTO> result = systemSettingService.getAllSettings();
        return ApiResponse.success(result);
    }

    @GetMapping("/{key}")
    @Operation(summary = "Chi tiết cài đặt", description = "Lấy cài đặt theo key")
    public ApiResponse<SystemSettingDTO> getByKey(@PathVariable String key) {
        log.info("Getting setting by key: {}", key);
        SystemSettingDTO result = systemSettingService.getByKey(key);
        if (result == null) {
            return ApiResponse.success("Không tìm thấy cài đặt với key: " + key, null);
        }
        return ApiResponse.success(result);
    }

    @PutMapping("/{key}")
    @Operation(summary = "Cập nhật cài đặt", description = "Cập nhật giá trị cài đặt theo key")
    public ApiResponse<SystemSettingDTO> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody SystemSettingUpdateDTO request) {
        log.info("Updating setting key: {} with value: {}", key, request.getSettingValue());
        SystemSettingDTO result = systemSettingService.setValue(
                key, 
                request.getSettingValue(), 
                request.getDescription()
        );
        return ApiResponse.success("Cập nhật cài đặt thành công", result);
    }

    @PostMapping
    @Operation(summary = "Tạo cài đặt mới", description = "Tạo cài đặt hệ thống mới")
    public ApiResponse<SystemSettingDTO> createSetting(@Valid @RequestBody SystemSettingDTO request) {
        log.info("Creating new setting: {}", request.getSettingKey());
        SystemSettingDTO result = systemSettingService.setValue(
                request.getSettingKey(),
                request.getSettingValue(),
                request.getDescription()
        );
        return ApiResponse.success("Tạo cài đặt thành công", result);
    }

    @DeleteMapping("/{key}")
    @Operation(summary = "Xóa cài đặt", description = "Xóa cài đặt theo key")
    public ApiResponse<Void> deleteSetting(@PathVariable String key) {
        log.info("Deleting setting key: {}", key);
        systemSettingService.deleteByKey(key);
        return ApiResponse.success("Xóa cài đặt thành công");
    }
}
