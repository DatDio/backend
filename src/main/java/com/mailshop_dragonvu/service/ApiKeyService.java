package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.request.ApiKeyGenerateRequest;
import com.mailshop_dragonvu.dto.response.ApiKeyGeneratedResponse;
import com.mailshop_dragonvu.dto.response.ApiKeyResponse;
import com.mailshop_dragonvu.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * API Key Service Interface
 */
public interface ApiKeyService {

    /**
     * Generate a new API key for a user
     */
    ApiKeyGeneratedResponse generateApiKey(ApiKeyGenerateRequest request, Long userId);

    /**
     * Revoke (deactivate) an API key
     */
    ApiKeyResponse revokeApiKey(UUID keyId, Long userId);

    /**
     * Activate an API key
     */
    ApiKeyResponse activateApiKey(UUID keyId, Long userId);

    /**
     * Get all API keys for a user
     */
    List<ApiKeyResponse> getUserApiKeys(Long userId);

    /**
     * Get API key by ID
     */
    ApiKeyResponse getApiKeyById(UUID keyId, Long userId);

    /**
     * Validate API key and return associated user
     */
    User validateApiKey(String apiKey);

    /**
     * Update last used timestamp for an API key
     */
    void updateLastUsed(UUID keyId);

    /**
     * Deactivate expired API keys (scheduled task)
     */
    void deactivateExpiredKeys();

    /**
     * Get usage statistics for an API key
     */
    ApiKeyResponse getUsageStats(UUID keyId, Long userId);
}
