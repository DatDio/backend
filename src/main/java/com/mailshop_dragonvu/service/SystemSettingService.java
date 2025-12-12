package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.dto.settings.SystemSettingDTO;

import java.util.List;

public interface SystemSettingService {

    /**
     * Get all system settings
     */
    List<SystemSettingDTO> getAllSettings();

    /**
     * Get setting value by key
     */
    String getValue(String key);

    /**
     * Get setting value by key with default value
     */
    String getValue(String key, String defaultValue);

    /**
     * Get setting value as Integer
     */
    Integer getIntValue(String key, Integer defaultValue);

    /**
     * Get setting value as Long
     */
    Long getLongValue(String key, Long defaultValue);

    /**
     * Get setting value as Boolean
     */
    Boolean getBooleanValue(String key, Boolean defaultValue);

    /**
     * Set/Update setting value
     */
    SystemSettingDTO setValue(String key, String value);

    /**
     * Set/Update setting with description
     */
    SystemSettingDTO setValue(String key, String value, String description);

    /**
     * Delete setting by key
     */
    void deleteByKey(String key);

    /**
     * Get setting DTO by key
     */
    SystemSettingDTO getByKey(String key);
}
