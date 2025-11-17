package com.mailshop_dragonvu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Application Configuration
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate bean for HTTP requests (PayOS API calls)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
