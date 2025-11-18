package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.apikeys.ApiKeyGenerateRequest;
import com.mailshop_dragonvu.dto.apikeys.ApiKeyGeneratedResponse;
import com.mailshop_dragonvu.dto.apikeys.ApiKeyResponse;
import com.mailshop_dragonvu.entity.ApiKey;
import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.enums.ApiKeyStatus;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.ApiKeyMapper;
import com.mailshop_dragonvu.repository.ApiKeyRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.service.ApiKeyService;
import com.mailshop_dragonvu.util.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * API Key Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final ApiKeyGenerator apiKeyGenerator;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_ACTIVE_KEYS_PER_USER = 5;

    @Override
    @Transactional
    @CacheEvict(value = "apikeys", allEntries = true)
    public ApiKeyGeneratedResponse generateApiKey(ApiKeyGenerateRequest request, Long userId) {
        log.info("Generating new API key for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Check if user has reached max active keys limit
        Long activeKeyCount = apiKeyRepository.countActiveKeysByUserId(userId);
        if (activeKeyCount >= MAX_ACTIVE_KEYS_PER_USER) {
            throw new BusinessException(ErrorCode.API_KEY_LIMIT_REACHED,
                    "Maximum active API keys limit reached (" + MAX_ACTIVE_KEYS_PER_USER + ")");
        }

        // Generate plaintext API key
        String plaintextKey = apiKeyGenerator.generateApiKey();

        // Mã hóa apikey trước khi lưu vào db
        String keyHash = passwordEncoder.encode(plaintextKey);


        ApiKey apiKey = ApiKey.builder()
            .user(user)
            .keyHash(keyHash)
            .name(request.getName())
            .status(ApiKeyStatus.ACTIVE)
            .expiredAt(request.getExpiredAt())
            .build();

        apiKey = apiKeyRepository.save(apiKey);

        ApiKeyResponse metadata = apiKeyMapper.toResponse(apiKey);
        return ApiKeyGeneratedResponse.of(metadata, plaintextKey);
    }

    @Override
    @Transactional
    @CacheEvict(value = "apikeys", allEntries = true)
    public ApiKeyResponse revokeApiKey(Long keyId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ApiKey apiKey = apiKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND));

        apiKey.setStatus(ApiKeyStatus.INACTIVE);
        apiKey = apiKeyRepository.save(apiKey);

        return apiKeyMapper.toResponse(apiKey);
    }

    @Override
    @Transactional
    @CacheEvict(value = "apikeys", allEntries = true)
    public ApiKeyResponse activateApiKey(Long keyId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ApiKey apiKey = apiKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND));

        // Check if key is expired
        if (apiKey.isExpired()) {
            throw new BusinessException(ErrorCode.API_KEY_EXPIRED);
        }

        // Check active keys limit
        Long activeKeyCount = apiKeyRepository.countActiveKeysByUserId(userId);
        if (activeKeyCount >= MAX_ACTIVE_KEYS_PER_USER && apiKey.getStatus() != ApiKeyStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.API_KEY_LIMIT_REACHED,
                    "Maximum active API keys limit reached (" + MAX_ACTIVE_KEYS_PER_USER + ")");
        }

        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        apiKey = apiKeyRepository.save(apiKey);

        return apiKeyMapper.toResponse(apiKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getUserApiKeys(Long userId) {
        log.debug("Fetching all API keys for user ID: {}", userId);

        List<ApiKey> apiKeys = apiKeyRepository.findByUserId(userId);
        return apiKeys.stream()
                .map(apiKeyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiKeyResponse getApiKeyById(Long keyId, Long userId) {
        log.debug("Fetching API key by ID: {} for user ID: {}", keyId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ApiKey apiKey = apiKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND));

        return apiKeyMapper.toResponse(apiKey);
    }

    @Override
    @Transactional(readOnly = true)
    public User validateApiKey(String apiKey) {
        log.debug("Validating API key");

        // Validate format first
        if (!apiKeyGenerator.isValidFormat(apiKey)) {
            log.warn("Invalid API key format");
            throw new BusinessException(ErrorCode.API_KEY_INVALID);
        }

        // Get all active API keys and check against the provided key
        List<ApiKey> activeKeys = apiKeyRepository.findAll().stream()
                .filter(key -> key.getStatus() == ApiKeyStatus.ACTIVE)
                .collect(Collectors.toList());

        for (ApiKey key : activeKeys) {
            if (passwordEncoder.matches(apiKey, key.getKeyHash())) {
                // Check if key is expired
                if (key.isExpired()) {
                    log.warn("API key is expired: {}", key.getId());
                    throw new BusinessException(ErrorCode.API_KEY_EXPIRED);
                }

                log.info("API key validated successfully for user: {}", key.getUser().getId());
                return key.getUser();
            }
        }

        log.warn("Invalid API key provided");
        throw new BusinessException(ErrorCode.API_KEY_INVALID);
    }

    @Override
    @Transactional
    public void updateLastUsed(Long keyId) {
        apiKeyRepository.findById(keyId).ifPresent(apiKey -> {
            apiKey.updateLastUsed();
            apiKeyRepository.save(apiKey);
        });
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void deactivateExpiredKeys() {
        log.info("Checking for expired API keys...");

        List<ApiKey> expiredKeys = apiKeyRepository.findExpiredActiveKeys();

        for (ApiKey key : expiredKeys) {
            key.setStatus(ApiKeyStatus.INACTIVE);
            apiKeyRepository.save(key);
        }

        log.info("Deactivated {} expired API keys", expiredKeys.size());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiKeyResponse getUsageStats(Long keyId, Long userId) {
        log.debug("Fetching usage stats for API key ID: {}", keyId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ApiKey apiKey = apiKeyRepository.findByIdAndUser(keyId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND));

        // For now, just return the metadata
        // In the future, this can include request counts, rate limiting info, etc.
        return apiKeyMapper.toResponse(apiKey);
    }
}
