package com.mailshop_dragonvu.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import org.springframework.context.i18n.LocaleContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Configuration for internationalization (i18n) support.
 * Supports Vietnamese (default) and English languages.
 * 
 * Language can be specified via:
 * - Query parameter: ?lang=vi or ?lang=en
 * - Accept-Language header
 */
@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    /**
     * Custom LocaleResolver that supports both query parameter and Accept-Language header.
     * Priority: Query param > Accept-Language header > Default (vi)
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new LocaleResolver() {
            private final Locale defaultLocale = new Locale("vi");
            
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                // 1. Check query parameter first
                String langParam = request.getParameter("lang");
                if (langParam != null && !langParam.isEmpty()) {
                    if ("en".equalsIgnoreCase(langParam)) {
                        return Locale.ENGLISH;
                    } else if ("vi".equalsIgnoreCase(langParam)) {
                        return new Locale("vi");
                    }
                }
                
                // 2. Check Accept-Language header
                String acceptLanguage = request.getHeader("Accept-Language");
                if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
                    if (acceptLanguage.toLowerCase().startsWith("en")) {
                        return Locale.ENGLISH;
                    } else if (acceptLanguage.toLowerCase().startsWith("vi")) {
                        return new Locale("vi");
                    }
                }
                
                // 3. Return default locale (Vietnamese)
                return defaultLocale;
            }
            
            @Override
            public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
                // Update LocaleContextHolder so MessageService can use the correct locale
                if (locale != null) {
                    LocaleContextHolder.setLocale(locale);
                }
            }
        };
    }

    /**
     * MessageSource for loading messages from properties files.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(new Locale("vi"));
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * Interceptor for changing locale based on 'lang' query parameter.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
