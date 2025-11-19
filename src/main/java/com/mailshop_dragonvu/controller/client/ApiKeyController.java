package com.mailshop_dragonvu.controller.client;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.dto.apikeys.ApiKeyGenerateRequest;
import com.mailshop_dragonvu.dto.apikeys.ApiKeyGeneratedResponse;
import com.mailshop_dragonvu.dto.apikeys.ApiKeyResponse;
import com.mailshop_dragonvu.security.UserPrincipal;
import com.mailshop_dragonvu.service.ApiKeyService;
import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API Key Controller - manages user API keys
 */
@RestController
@RequestMapping(Constants.API_PATH.APIKEY)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Key Management", description = "User API key operations for authentication")
@SecurityRequirement(name = "Bearer Authentication")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate new API key", description = "Generate a new API key for the authenticated user. The plaintext key is shown only once.")
    public ResponseEntity<ApiResponse<ApiKeyGeneratedResponse>> generateApiKey(
            @Valid @RequestBody ApiKeyGenerateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ApiKeyGeneratedResponse response = apiKeyService.generateApiKey(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/revoke/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Revoke API key", description = "Deactivate an existing API key")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> revokeApiKey(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        log.info("User {} revoking API key {}", currentUser.getId(), id);
        ApiKeyResponse response = apiKeyService.revokeApiKey(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/activate/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Activate API key", description = "Activate an inactive API key")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> activateApiKey(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        log.info("User {} activating API key {}", currentUser.getId(), id);
        ApiKeyResponse response = apiKeyService.activateApiKey(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List API keys", description = "Get all API keys for the authenticated user")
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> listApiKeys(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        log.info("User {} listing API keys", currentUser.getId());
        List<ApiKeyResponse> response = apiKeyService.getUserApiKeys(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/usage-stats/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get API key usage statistics", description = "Get usage statistics for a specific API key (placeholder for future implementation)")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> getUsageStats(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        log.info("User {} fetching usage stats for API key {}", currentUser.getId(), id);
        ApiKeyResponse response = apiKeyService.getUsageStats(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get API key by ID", description = "Get API key metadata by ID")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> getApiKeyById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        log.info("User {} fetching API key {}", currentUser.getId(), id);
        ApiKeyResponse response = apiKeyService.getApiKeyById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
