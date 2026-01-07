package com.mailshop_dragonvu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for retrieving localized messages based on current locale.
 * Supports Vietnamese (default) and English languages.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Get message by code using current request's locale.
     * 
     * @param code Message key
     * @return Localized message
     */
    public String getMessage(String code) {
        return getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get message by code with arguments using current request's locale.
     * 
     * @param code Message key
     * @param args Arguments to be inserted into message placeholders
     * @return Localized message with arguments replaced
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * Get message by code with arguments and specific locale.
     * 
     * @param code   Message key
     * @param args   Arguments to be inserted into message placeholders
     * @param locale Specific locale to use
     * @return Localized message
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            // Fallback to code if message not found
            return code;
        }
    }

    /**
     * Get error message by error code.
     * Automatically prepends "error." prefix to the code.
     * 
     * @param errorCode Error code (e.g., "1000", "3001")
     * @return Localized error message
     */
    public String getErrorMessage(String errorCode) {
        return getMessage("error." + errorCode);
    }

    /**
     * Get error message by error code with arguments.
     * 
     * @param errorCode Error code
     * @param args      Arguments for message placeholders
     * @return Localized error message with arguments
     */
    public String getErrorMessage(String errorCode, Object[] args) {
        return getMessage("error." + errorCode, args);
    }

    /**
     * Get success message by key.
     * Automatically prepends "success." prefix.
     * 
     * @param key Success message key
     * @return Localized success message
     */
    public String getSuccessMessage(String key) {
        return getMessage("success." + key);
    }

    /**
     * Get validation message by key.
     * Automatically prepends "validation." prefix.
     * 
     * @param key Validation message key
     * @return Localized validation message
     */
    public String getValidationMessage(String key) {
        return getMessage("validation." + key);
    }

    /**
     * Get validation message by key with arguments.
     * 
     * @param key  Validation message key
     * @param args Arguments for placeholders
     * @return Localized validation message
     */
    public String getValidationMessage(String key, Object[] args) {
        return getMessage("validation." + key, args);
    }

    /**
     * Get current locale from the request context.
     * 
     * @return Current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Check if current locale is Vietnamese.
     * 
     * @return true if current locale is Vietnamese
     */
    public boolean isVietnamese() {
        return "vi".equalsIgnoreCase(getCurrentLocale().getLanguage());
    }

    /**
     * Check if current locale is English.
     * 
     * @return true if current locale is English
     */
    public boolean isEnglish() {
        return "en".equalsIgnoreCase(getCurrentLocale().getLanguage());
    }
}
