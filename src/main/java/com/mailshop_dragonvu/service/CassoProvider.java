package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Casso Provider - Provides Casso configuration from database or environment
 * Similar to PayOSProvider, supports dynamic configuration via admin settings
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CassoProvider {

    private final SystemSettingService systemSettingService;

    // Fallback values from environment
    @Value("${casso.secure-token:}")
    private String envSecureToken;

    @Value("${casso.bank-code:}")
    private String envBankCode;

    @Value("${casso.bank-account:}")
    private String envBankAccount;

    @Value("${casso.account-name:}")
    private String envAccountName;

    // Setting keys in database
    public static final String KEY_SECURE_TOKEN = "casso.secure_token";
    public static final String KEY_BANK_CODE = "casso.bank_code";
    public static final String KEY_BANK_ACCOUNT = "casso.bank_account";
    public static final String KEY_ACCOUNT_NAME = "casso.account_name";

    /**
     * Get Secure Token for webhook verification
     * Priority: Database > Environment
     */
    public String getSecureToken() {
        String dbValue = systemSettingService.getValue(KEY_SECURE_TOKEN);
        return Strings.isNotBlank(dbValue) ? dbValue : envSecureToken;
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
     * Check if Casso is properly configured for deposits
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
        return Strings.isNotBlank(getSecureToken());
    }

    /**
     * Validate configuration or throw exception
     */
    public void validateConfiguration() {
        if (!isConfigured()) {
            log.error("Casso bank configuration not set. Please configure in Settings.");
            throw new BusinessException(ErrorCode.PAYMENT_CONFIGURATION_ERROR);
        }
    }
}
