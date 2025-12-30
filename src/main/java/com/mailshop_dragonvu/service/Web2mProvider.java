package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Web2m Provider - Provides Web2m configuration from database or environment
 * Similar to PayOSProvider, supports dynamic configuration via admin settings
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Web2mProvider {

    private final SystemSettingService systemSettingService;

    // Fallback values from environment
    @Value("${web2m.access-token:}")
    private String envAccessToken;

    @Value("${web2m.bank-code:}")
    private String envBankCode;

    @Value("${web2m.bank-account:}")
    private String envBankAccount;

    @Value("${web2m.account-name:}")
    private String envAccountName;

    // Setting keys in database
    public static final String KEY_ACCESS_TOKEN = "web2m.access_token";
    public static final String KEY_BANK_CODE = "web2m.bank_code";
    public static final String KEY_BANK_ACCOUNT = "web2m.bank_account";
    public static final String KEY_ACCOUNT_NAME = "web2m.account_name";

    /**
     * Get Access Token for webhook verification
     * Priority: Database > Environment
     */
    public String getAccessToken() {
        String dbValue = systemSettingService.getValue(KEY_ACCESS_TOKEN);
        return Strings.isNotBlank(dbValue) ? dbValue : envAccessToken;
    }

    /**
     * Get Bank Code (e.g., ACB, VCB, TCB...)
     */
    public String getBankCode() {
        String dbValue = systemSettingService.getValue(KEY_BANK_CODE);
        return Strings.isNotBlank(dbValue) ? dbValue : envBankCode;
    }

    /**
     * Get Bank Account Number
     */
    public String getBankAccount() {
        String dbValue = systemSettingService.getValue(KEY_BANK_ACCOUNT);
        return Strings.isNotBlank(dbValue) ? dbValue : envBankAccount;
    }

    /**
     * Get Account Holder Name (no diacritics, uppercase)
     */
    public String getAccountName() {
        String dbValue = systemSettingService.getValue(KEY_ACCOUNT_NAME);
        return Strings.isNotBlank(dbValue) ? dbValue : envAccountName;
    }

    /**
     * Check if Web2m is properly configured for deposits
     */
    public boolean isConfigured() {
        return Strings.isNotBlank(getBankCode()) 
            && Strings.isNotBlank(getBankAccount()) 
            && Strings.isNotBlank(getAccountName());
    }

    /**
     * Check if webhook verification is configured
     */
    public boolean isWebhookConfigured() {
        return Strings.isNotBlank(getAccessToken());
    }

    /**
     * Validate configuration or throw exception
     */
    public void validateConfiguration() {
        if (!isConfigured()) {
            log.error("Web2m bank configuration not set. Please configure in Settings.");
            throw new BusinessException(ErrorCode.PAYMENT_CONFIGURATION_ERROR);
        }
    }
}
