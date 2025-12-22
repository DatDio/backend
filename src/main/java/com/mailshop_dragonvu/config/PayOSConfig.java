package com.mailshop_dragonvu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * PayOS Configuration
 * 
 * NOTE: PayOS instance is now created dynamically by PayOSProvider
 * which reads credentials from database (SystemSettings) with fallback to environment variables.
 * 
 * Environment variables are still used as fallback:
 * - payos.client-id
 * - payos.api-key
 * - payos.checksum-key
 */
@Configuration
public class PayOSConfig {
    // These are now only used as fallback by PayOSProvider
    @Value("${payos.client-id:}")
    private String clientId;

    @Value("${payos.api-key:}")
    private String apiKey;

    @Value("${payos.checksum-key:}")
    private String checksumKey;

    // PayOS bean is no longer needed - PayOSProvider creates instances dynamically
    // @Bean
    // public PayOS payOS() {
    //     return new PayOS(clientId, apiKey, checksumKey);
    // }
}

