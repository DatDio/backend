package com.mailshop_dragonvu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration to serve uploaded files from external directory
 * Files saved to 'uploads/' folder will be accessible via '/uploads/' URL path
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path of upload directory
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        String uploadAbsolutePath = uploadDir.toUri().toString();
        
        // Serve files from /uploads/** URL path
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadAbsolutePath);
    }
}
