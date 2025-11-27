package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.apikeys.ApiKeyGenerateRequest;
import com.mailshop_dragonvu.dto.apikeys.ApiKeyGeneratedResponse;
import com.mailshop_dragonvu.dto.apikeys.ApiKeyResponse;
import com.mailshop_dragonvu.entity.ApiKeyEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.ApiKeyStatusEnum;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.ApiKeyMapper;
import com.mailshop_dragonvu.repository.ApiKeyRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.service.ApiKeyService;
import com.mailshop_dragonvu.utils.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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

        UserEntity userEntity = userRepository.findById(userId)
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


        ApiKeyEntity apiKeyEntity = com.mailshop_dragonvu.entity.ApiKeyEntity.builder()
            .user(userEntity)
            .keyHash(keyHash)
            .name( Optional.ofNullable(request.getName())
                    .filter(name -> !name.isBlank())
                    .orElse(userEntity.getEmail()))
            .status(ApiKeyStatusEnum.ACTIVE)
            .build();

        apiKeyEntity = apiKeyRepository.save(apiKeyEntity);

        ApiKeyResponse metadata = apiKeyMapper.toResponse(apiKeyEntity);
        return ApiKeyGeneratedResponse.of(metadata, plaintextKey);
    }

    @Override
    @Transactional
    @CacheEvict(value = "apikeys", allEntries = true)
    public ApiKeyResponse revokeApiKey(Long keyId, Long userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ApiKeyEntity apiKeyEntity = apiKeyRepository.findByIdAndUser(keyId, userEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND));

        apiKeyEntity.setStatus(ApiKeyStatusEnum.INACTIVE);
        apiKeyEntity = apiKeyRepository.save(apiKeyEntity);

        return apiKeyMapper.toResponse(apiKeyEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "apikeys", allEntries = true)
    public ApiKeyResponse activateApiKey(Long keyId, Long userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ApiKeyEntity apiKeyEntity = apiKeyRepository.findByIdAndUser(keyId, userEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND));



        // Check active keys limit
        Long activeKeyCount = apiKeyRepository.countActiveKeysByUserId(userId);
        if (activeKeyCount >= MAX_ACTIVE_KEYS_PER_USER && apiKeyEntity.getStatus() != ApiKeyStatusEnum.ACTIVE) {
            throw new BusinessException(ErrorCode.API_KEY_LIMIT_REACHED,
                    "Maximum active API keys limit reached (" + MAX_ACTIVE_KEYS_PER_USER + ")");
        }

        apiKeyEntity.setStatus(ApiKeyStatusEnum.ACTIVE);
        apiKeyEntity = apiKeyRepository.save(apiKeyEntity);

        return apiKeyMapper.toResponse(apiKeyEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getUserApiKeys(Long userId) {
        log.debug("Fetching all API keys for user ID: {}", userId);

        List<ApiKeyEntity> apiKeyEntities = apiKeyRepository.findByUserId(userId);
        return apiKeyEntities.stream()
                .map(apiKeyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiKeyResponse getApiKeyById(Long keyId, Long userId) {
        log.debug("Fetching API key by ID: {} for user ID: {}", keyId, userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ApiKeyEntity apiKeyEntity = apiKeyRepository.findByIdAndUser(keyId, userEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND));

        return apiKeyMapper.toResponse(apiKeyEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity validateApiKey(String apiKey) {
        log.debug("Validating API key");

        // Validate format first
        if (!apiKeyGenerator.isValidFormat(apiKey)) {
            log.warn("Invalid API key format");
            throw new BusinessException(ErrorCode.API_KEY_INVALID);
        }

        // Get all active API keys and check against the provided key
        List<ApiKeyEntity> activeKeys = apiKeyRepository.findAll().stream()
                .filter(key -> key.getStatus() == ApiKeyStatusEnum.ACTIVE)
                .collect(Collectors.toList());

        for (ApiKeyEntity key : activeKeys) {
            if (passwordEncoder.matches(apiKey, key.getKeyHash())) {
                return key.getUser();
            }
        }

        log.warn("Invalid API key provided");
        throw new BusinessException(ErrorCode.API_KEY_INVALID);
    }

    @Override
    @Transactional
    public void updateLastUsed(Long keyId) {
        apiKeyRepository.findById(keyId).ifPresent(apiKeyEntity -> {
            apiKeyEntity.updateLastUsed();
            apiKeyRepository.save(apiKeyEntity);
        });
    }

    @Override
    @Transactional
    public void deleteApiKey(Long keyId) {
        apiKeyRepository.delete(apiKeyRepository.findById(keyId).orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND)));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiKeyResponse getUsageStats(Long keyId, Long userId) {
        log.debug("Fetching usage stats for API key ID: {}", keyId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ApiKeyEntity apiKeyEntity = apiKeyRepository.findByIdAndUser(keyId, userEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND));

        // For now, just return the metadata
        // In the future, this can include request counts, rate limiting info, etc.
        return apiKeyMapper.toResponse(apiKeyEntity);
    }
}
