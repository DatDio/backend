package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.settings.SystemSettingDTO;
import com.mailshop_dragonvu.entity.SystemSettingEntity;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.repository.SystemSettingRepository;
import com.mailshop_dragonvu.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SystemSettingDTO> getAllSettings() {
        return systemSettingRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String getValue(String key) {
        return systemSettingRepository.findBySettingKey(key)
                .map(SystemSettingEntity::getSettingValue)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public String getValue(String key, String defaultValue) {
        String value = getValue(key);
        return value != null ? value : defaultValue;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getIntValue(String key, Integer defaultValue) {
        String value = getValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value for key '{}': {}", key, value);
            return defaultValue;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLongValue(String key, Long defaultValue) {
        String value = getValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid long value for key '{}': {}", key, value);
            return defaultValue;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        String value = getValue(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    @Override
    public SystemSettingDTO setValue(String key, String value) {
        return setValue(key, value, null);
    }

    @Override
    public SystemSettingDTO setValue(String key, String value, String description) {
        log.info("Setting value for key '{}': {}", key, value);

        SystemSettingEntity entity = systemSettingRepository.findBySettingKey(key)
                .orElse(SystemSettingEntity.builder()
                        .settingKey(key)
                        .build());

        entity.setSettingValue(value);
        entity.setDescription(description);

        entity = systemSettingRepository.save(entity);
        log.info("Setting saved successfully for key '{}'", key);

        return toDTO(entity);
    }

    @Override
    public void deleteByKey(String key) {
        log.info("Deleting setting with key '{}'", key);
        SystemSettingEntity entity = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new BusinessException("Không tìm thấy setting với key: " + key));
        systemSettingRepository.delete(entity);
        log.info("Setting deleted successfully for key '{}'", key);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemSettingDTO getByKey(String key) {
        return systemSettingRepository.findBySettingKey(key)
                .map(this::toDTO)
                .orElse(null);
    }

    private SystemSettingDTO toDTO(SystemSettingEntity entity) {
        if (entity == null) return null;

        return SystemSettingDTO.builder()
                .id(entity.getId())
                .settingKey(entity.getSettingKey())
                .settingValue(entity.getSettingValue())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
