package com.mailshop_dragonvu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API Key Generated Response DTO - includes plaintext key (shown only once)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyGeneratedResponse {

    private ApiKeyResponse keyMetadata;
    
    /**
     * Plaintext API key - shown only once during generation
     */
    private String apiKey;
    
    private String warning;

    public static ApiKeyGeneratedResponse of(ApiKeyResponse metadata, String apiKey) {
        return ApiKeyGeneratedResponse.builder()
                .keyMetadata(metadata)
                .apiKey(apiKey)
                .warning("This is the only time you will see this API key. Please store it securely.")
                .build();
    }
}
