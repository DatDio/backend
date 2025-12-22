package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;

/**
 * PayOS Provider - Creates PayOS instance dynamically from database settings
 * Falls back to environment variables if not configured in database
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayOSProvider {

    private final SystemSettingService systemSettingService;

    // Fallback values from environment
    @Value("${payos.client-id:}")
    private String envClientId;

    @Value("${payos.api-key:}")
    private String envApiKey;

    @Value("${payos.checksum-key:}")
    private String envChecksumKey;

    // Setting keys in database
    public static final String KEY_CLIENT_ID = "payos.client_id";
    public static final String KEY_API_KEY = "payos.api_key";
    public static final String KEY_CHECKSUM_KEY = "payos.checksum_key";

    /**
     * Get PayOS instance with current configuration
     * Priority: Database settings > Environment variables
     */
    public PayOS getPayOS() {
        String clientId = getClientId();
        String apiKey = getApiKey();
        String checksumKey = getChecksumKey();

        if (Strings.isBlank(clientId) || Strings.isBlank(apiKey) || Strings.isBlank(checksumKey)) {
            log.error("PayOS credentials not configured. Please configure in Settings or environment variables.");
            throw new BusinessException(ErrorCode.PAYMENT_CONFIGURATION_ERROR);
        }

        return new PayOS(clientId, apiKey, checksumKey);
    }

    /**
     * Get Client ID - from DB first, fallback to env
     */
    public String getClientId() {
        String dbValue = systemSettingService.getValue(KEY_CLIENT_ID);
        return Strings.isNotBlank(dbValue) ? dbValue : envClientId;
    }

    /**
     * Get API Key - from DB first, fallback to env
     */
    public String getApiKey() {
        String dbValue = systemSettingService.getValue(KEY_API_KEY);
        return Strings.isNotBlank(dbValue) ? dbValue : envApiKey;
    }

    /**
     * Get Checksum Key - from DB first, fallback to env
     */
    public String getChecksumKey() {
        String dbValue = systemSettingService.getValue(KEY_CHECKSUM_KEY);
        return Strings.isNotBlank(dbValue) ? dbValue : envChecksumKey;
    }

    /**
     * Check if PayOS is properly configured
     */
    public boolean isConfigured() {
        String clientId = getClientId();
        String apiKey = getApiKey();
        String checksumKey = getChecksumKey();
        return Strings.isNotBlank(clientId) && Strings.isNotBlank(apiKey) && Strings.isNotBlank(checksumKey);
    }
}
