package com.mailshop_dragonvu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.util.SecurityUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global Rate Limiting Filter
 * Chống DDoS cho toàn bộ application
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    
    // Cache buckets per IP
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Rate limit config: 100 requests per minute per IP
    private static final int CAPACITY = 100;
    private static final int REFILL_TOKENS = 100;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String ip = SecurityUtils.getClientIp(request);
        String path = request.getRequestURI();
        
        // Skip rate limiting for health check and actuator endpoints
        if (path.startsWith("/actuator") || path.equals("/api/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = resolveBucket(ip);
        
        if (bucket.tryConsume(1)) {
            // Request allowed
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for IP: {} on path: {}", ip, path);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            ApiResponse<Object> errorResponse = ApiResponse.error(
                ErrorCode.RATE_LIMIT_EXCEEDED.getCode(),
                "Too many requests. Please try again later."
            );
            
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    /**
     * Get or create bucket for IP
     */
    private Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, k -> createNewBucket());
    }

    /**
     * Create new bucket with rate limit configuration
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(CAPACITY, Refill.intervally(REFILL_TOKENS, REFILL_DURATION));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Clean up old buckets periodically (optional)
     */
    public void cleanup() {
        if (cache.size() > 10000) {
            log.info("Cleaning up rate limit cache. Current size: {}", cache.size());
            cache.clear();
        }
    }
}
