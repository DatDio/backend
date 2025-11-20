package com.mailshop_dragonvu.dto.apikeys;

import com.mailshop_dragonvu.enums.ApiKeyStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API Key Response DTO - does not include the actual key or hash
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {

    private Long id;
    private String name;
    private ApiKeyStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime lastUsedAt;
}
