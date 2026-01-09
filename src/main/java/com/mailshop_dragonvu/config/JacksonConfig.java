package com.mailshop_dragonvu.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson Configuration - Tăng giới hạn cho JSON parsing
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Đăng ký module xử lý Java 8 Date/Time (LocalDateTime, LocalDate, etc.)
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Tăng giới hạn độ dài string lên 100MB (100 * 1024 * 1024 bytes)
        JsonFactory factory = objectMapper.getFactory();
        factory.setStreamReadConstraints(
            StreamReadConstraints.builder()
                .maxStringLength(100 * 1024 * 1024) // 100MB
                .build()
        );
        
        return objectMapper;
    }
}
