package com.mailshop_dragonvu.security;

import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API Key Authentication Filter
 * Authenticates requests using X-API-KEY header
 * Works independently from JWT authentication
 * If both JWT and API-KEY are present, JWT takes priority
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    
    private final ApiKeyService apiKeyService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // Skip if already authenticated (JWT has priority)
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.debug("Request already authenticated, skipping API key authentication");
                filterChain.doFilter(request, response);
                return;
            }

            // Extract API key from header
            String apiKey = extractApiKeyFromRequest(request);

            if (StringUtils.hasText(apiKey)) {
                log.debug("API key found in request header");

                // Validate API key and get associated user
                User user = apiKeyService.validateApiKey(apiKey);

                if (user != null) {
                    log.info("API key validated for user: {}", user.getEmail());

                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("API key authentication successful for user: {}", user.getEmail());
                } else {
                    log.warn("API key validation returned null user");
                }
            }
        } catch (Exception e) {
            log.error("Cannot set API key authentication: {}", e.getMessage());
            // Don't throw exception, just let the request continue without authentication
            // The security configuration will handle unauthorized access
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract API key from X-API-KEY header
     */
    private String extractApiKeyFromRequest(HttpServletRequest request) {
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (StringUtils.hasText(apiKey)) {
            return apiKey.trim();
        }

        return null;
    }
}
