package com.mailshop_dragonvu.dto.request;

import com.mailshop_dragonvu.enums.ApiKeyPermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API Key Generate Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyGenerateRequest {

    @NotBlank(message = "API key name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Builder.Default
    private ApiKeyPermission permission = ApiKeyPermission.READ_ONLY;

    private LocalDateTime expiredAt;
}
