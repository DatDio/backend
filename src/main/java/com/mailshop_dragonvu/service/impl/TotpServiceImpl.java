package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.totp.TotpResponseDTO;
import com.mailshop_dragonvu.service.TotpService;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service implementation for TOTP 2FA code generation using RFC 6238 standard
 */
@Service
@Slf4j
public class TotpServiceImpl implements TotpService {

    private static final int TIME_PERIOD = 30; // Standard TOTP time period in seconds
    private static final int CODE_DIGITS = 6;  // Standard 6-digit code

    private final TimeProvider timeProvider;
    private final CodeGenerator codeGenerator;

    public TotpServiceImpl() {
        this.timeProvider = new SystemTimeProvider();
        this.codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, CODE_DIGITS);
    }

    @Override
    public List<TotpResponseDTO> generateCodes(String secretData) {
        if (secretData == null || secretData.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<TotpResponseDTO> results = new ArrayList<>();
        String[] lines = secretData.split("\n");
        int timeRemaining = getTimeRemaining();

        for (String line : lines) {
            String secret = extractSecret(line.trim());
            
            if (secret.isEmpty()) {
                continue;
            }

            try {
                String code = generateCode(secret);
                
                if (code != null) {
                    results.add(TotpResponseDTO.builder()
                            .identifier(getIdentifier(line.trim(), secret))
                            .secret(secret)
                            .code(code)
                            .status("success")
                            .timeRemaining(timeRemaining)
                            .build());
                } else {
                    results.add(TotpResponseDTO.builder()
                            .identifier(getIdentifier(line.trim(), secret))
                            .secret(secret)
                            .status("error")
                            .error("Invalid secret key")
                            .timeRemaining(0)
                            .build());
                }
            } catch (Exception e) {
                log.error("Error generating TOTP for secret: {}", maskSecret(secret), e);
                results.add(TotpResponseDTO.builder()
                        .identifier(getIdentifier(line.trim(), secret))
                        .secret(secret)
                        .status("error")
                        .error("Failed to generate code: " + e.getMessage())
                        .timeRemaining(0)
                        .build());
            }
        }

        log.info("Generated {} TOTP codes", results.size());
        return results;
    }

    @Override
    public String generateCode(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            return null;
        }

        try {
            // Clean up secret - remove spaces and convert to uppercase
            String cleanSecret = secret.replaceAll("\\s+", "").toUpperCase();
            
            // Get current time bucket (counter)
            long currentBucket = Math.floorDiv(timeProvider.getTime(), TIME_PERIOD);
            
            // Generate code
            return codeGenerator.generate(cleanSecret, currentBucket);
        } catch (CodeGenerationException e) {
            log.error("Failed to generate TOTP code: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public int getTimeRemaining() {
        long currentSecond = timeProvider.getTime() % TIME_PERIOD;
        return (int) (TIME_PERIOD - currentSecond);
    }

    /**
     * Extract secret from input line
     * Supports formats:
     * - Just secret: ABCDEFGH123456
     * - email|password|secret: test@email.com|pass123|ABCDEFGH123456
     */
    private String extractSecret(String line) {
        if (line.isEmpty()) {
            return "";
        }

        String[] parts = line.split("\\|");
        
        // If format is email|password|secret, get the 3rd part
        if (parts.length >= 3) {
            return parts[2].trim();
        }
        
        // Otherwise, treat the whole line as secret
        return parts[0].trim();
    }

    /**
     * Get display identifier for the result
     */
    private String getIdentifier(String originalLine, String secret) {
        String[] parts = originalLine.split("\\|");
        
        // If email was provided, use it
        if (parts.length >= 3 && !parts[0].trim().isEmpty()) {
            return parts[0].trim();
        }
        
        // Otherwise, use truncated secret
        if (secret.length() > 10) {
            return secret.substring(0, 10) + "...";
        }
        return secret;
    }

    /**
     * Mask secret for logging (show only first 4 and last 4 characters)
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 8) {
            return "***";
        }
        return secret.substring(0, 4) + "..." + secret.substring(secret.length() - 4);
    }
}
