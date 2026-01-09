package com.mailshop_dragonvu.service;

import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecaptchaService {

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    @Value("${recaptcha.verify-url}")
    private String verifyUrl;

    @Value("${recaptcha.threshold:0.5}")
    private double threshold;

    private final RestTemplate restTemplate;
    private final MessageService messageService;

    /**
     * Verify reCAPTCHA token with Google API
     * @param token The reCAPTCHA token from frontend
     * @param action Expected action name (e.g., "login", "register")
     * @throws BusinessException if verification fails
     */
    public void verify(String token, String action) {
        if (!StringUtils.hasText(token)) {
            log.warn("reCAPTCHA token is empty for action: {}", action);
            throw new BusinessException(ErrorCode.BAD_REQUEST, messageService.getMessage(MessageKeys.Captcha.REQUIRED));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secretKey);
            params.add("response", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(verifyUrl, request, Map.class);

            if (response.getBody() == null) {
                log.error("Empty response from reCAPTCHA API");
                throw new BusinessException(ErrorCode.BAD_REQUEST, messageService.getMessage(MessageKeys.Captcha.VERIFY_FAILED));
            }

            Map<String, Object> body = response.getBody();
            Boolean success = (Boolean) body.get("success");
            Double score = body.get("score") != null ? ((Number) body.get("score")).doubleValue() : 0.0;
            String responseAction = (String) body.get("action");

            log.debug("reCAPTCHA response - success: {}, score: {}, action: {}", success, score, responseAction);

            if (!Boolean.TRUE.equals(success)) {
                log.warn("reCAPTCHA verification failed for action: {}", action);
                throw new BusinessException(ErrorCode.BAD_REQUEST, messageService.getMessage(MessageKeys.Captcha.FAILED));
            }

            if (score < threshold) {
                log.warn("reCAPTCHA score {} is below threshold {} for action: {}", score, threshold, action);
                throw new BusinessException(ErrorCode.SUSPICIOUS_ACTIVITY, messageService.getMessage(MessageKeys.Captcha.SUSPICIOUS));
            }

            // Optionally verify action matches (if provided by frontend)
            if (StringUtils.hasText(responseAction) && !responseAction.equals(action)) {
                log.warn("reCAPTCHA action mismatch - expected: {}, got: {}", action, responseAction);
                // We don't throw here, just log, as some implementations may not send action
            }

            log.info("reCAPTCHA verification passed for action: {} with score: {}", action, score);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying reCAPTCHA token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.BAD_REQUEST, messageService.getMessage(MessageKeys.Captcha.RETRY));
        }
    }
}

