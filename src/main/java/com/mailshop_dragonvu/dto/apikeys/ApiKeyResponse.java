package com.mailshop_dragonvu.dto.response;

import com.mailshop_dragonvu.enums.ApiKeyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API Key Response DTO - does not include the actual key or hash
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {

    private UUID id;
    private String name;
    private ApiKeyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime lastUsedAt;
    private boolean expired;
    private boolean valid;
}
