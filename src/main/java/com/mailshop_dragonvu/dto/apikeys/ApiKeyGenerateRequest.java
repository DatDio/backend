package com.mailshop_dragonvu.dto.apikeys;

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
    private String name;
}
