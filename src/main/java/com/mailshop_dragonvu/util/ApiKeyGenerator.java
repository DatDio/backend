package com.mailshop_dragonvu.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * API Key Generator Utility
 * Generates secure random API keys similar to OpenAI/Stripe format
 */
@Component
public class ApiKeyGenerator {

    private static final String PREFIX = "msk"; // MailShop Key
    private static final int KEY_LENGTH = 32; // 32 bytes = 256 bits
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a secure API key with format: msk_<base64_encoded_random_bytes>
     * Example: msk_3xK9pL2mN8qR5tV7wY1zB4cD6fG8hJ0k
     */
    public String generateApiKey() {
        byte[] randomBytes = new byte[KEY_LENGTH];
        secureRandom.nextBytes(randomBytes);
        
        String encodedKey = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
        
        return PREFIX + "_" + encodedKey;
    }

    /**
     * Validate API key format
     */
    public boolean isValidFormat(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }
        
        // Check if it starts with the correct prefix
        if (!apiKey.startsWith(PREFIX + "_")) {
            return false;
        }
        
        // Extract the key part after prefix
        String keyPart = apiKey.substring(PREFIX.length() + 1);
        
        // Check if it's valid base64 and has reasonable length
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(keyPart);
            return decoded.length >= 20; // At least 20 bytes
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get the prefix used for API keys
     */
    public String getPrefix() {
        return PREFIX;
    }
}
