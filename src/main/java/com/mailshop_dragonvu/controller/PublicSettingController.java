package com.mailshop_dragonvu.controller;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.settings.SystemSettingDTO;
import com.mailshop_dragonvu.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Settings", description = "Public settings endpoint (no auth required)")
public class PublicSettingController {

    private final SystemSettingService systemSettingService;

    // Keys that can be exposed publicly
    private static final List<String> PUBLIC_KEYS = List.of(
            "social.telegram_group",
            "social.telegram_channel",
            "fpayment.usd_vnd_rate"
    );

    @GetMapping("/public")
    @Operation(summary = "Get public settings", description = "Get settings that can be displayed publicly (footer links, etc.)")
    public ApiResponse<Map<String, String>> getPublicSettings() {
        log.debug("Getting public settings");
        
        Map<String, String> publicSettings = new HashMap<>();
        
        for (String key : PUBLIC_KEYS) {
            SystemSettingDTO setting = systemSettingService.getByKey(key);
            if (setting != null && setting.getSettingValue() != null) {
                publicSettings.put(key, setting.getSettingValue());
            }
        }
        
        // Add default values if not set
        publicSettings.putIfAbsent("social.telegram_group", "https://t.me/emailsieure");
        publicSettings.putIfAbsent("social.telegram_channel", "https://t.me/emailsieure_support");
        
        return ApiResponse.success(publicSettings);
    }
}
