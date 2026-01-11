package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * FPayment Provider - Provides FPayment configuration from database or environment
 * Similar to CassoProvider, supports dynamic configuration via admin settings
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FPaymentProvider {

    private final SystemSettingService systemSettingService;

    // Fallback values from environment
    @Value("${fpayment.merchant-id:}")
    private String envMerchantId;

    @Value("${fpayment.api-key:}")
    private String envApiKey;

    @Value("${fpayment.usd-vnd-rate:25000}")
    private Long envUsdVndRate;

    // Setting keys in database
    public static final String KEY_MERCHANT_ID = "fpayment.merchant_id";
    public static final String KEY_API_KEY = "fpayment.api_key";
    public static final String KEY_USD_VND_RATE = "fpayment.usd_vnd_rate";

    // FPayment API URL
    public static final String API_BASE_URL = "https://app.fpayment.net/api";
    public static final String API_ADD_INVOICE = API_BASE_URL + "/AddInvoice";
    public static final String API_GET_INVOICE_STATUS = API_BASE_URL + "/GetInvoiceStatus";

    /**
     * Get Merchant ID
     * Priority: Database > Environment
     */
    public String getMerchantId() {
        String dbValue = systemSettingService.getValue(KEY_MERCHANT_ID);
        return Strings.isNotBlank(dbValue) ? dbValue : envMerchantId;
    }

    /**
     * Get API Key
     * Priority: Database > Environment
     */
    public String getApiKey() {
        String dbValue = systemSettingService.getValue(KEY_API_KEY);
        return Strings.isNotBlank(dbValue) ? dbValue : envApiKey;
    }

    /**
     * Get USD to VND exchange rate
     * Default: 25,000 VND per 1 USDT
     */
    public Long getUsdVndRate() {
        String dbValue = systemSettingService.getValue(KEY_USD_VND_RATE);
        if (Strings.isNotBlank(dbValue)) {
            try {
                return Long.parseLong(dbValue);
            } catch (NumberFormatException e) {
                log.warn("Invalid USD/VND rate in database: {}, using default", dbValue);
            }
        }
        return envUsdVndRate;
    }

    /**
     * Check if FPayment is properly configured
     */
    public boolean isConfigured() {
        return Strings.isNotBlank(getMerchantId()) 
            && Strings.isNotBlank(getApiKey());
    }

    /**
     * Validate configuration or throw exception
     */
    public void validateConfiguration() {
        if (!isConfigured()) {
            log.error("FPayment configuration not set. Please configure Merchant ID and API Key in Settings.");
            throw new BusinessException(ErrorCode.PAYMENT_CONFIGURATION_ERROR);
        }
    }

    /**
     * Verify webhook credentials
     * @param merchantId Merchant ID from webhook
     * @param apiKey API Key from webhook
     * @return true if credentials match
     */
    public boolean verifyCredentials(String merchantId, String apiKey) {
        String configuredMerchantId = getMerchantId();
        String configuredApiKey = getApiKey();

        if (Strings.isBlank(configuredMerchantId) || Strings.isBlank(configuredApiKey)) {
            log.warn("FPayment credentials not configured");
            return false;
        }

        boolean isValid = configuredMerchantId.equals(merchantId) && configuredApiKey.equals(apiKey);
        if (!isValid) {
            log.warn("Invalid FPayment webhook credentials - merchantId: {}, expected: {}", 
                    merchantId, configuredMerchantId);
        }
        return isValid;
    }
}
