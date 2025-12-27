package com.mailshop_dragonvu.security;

import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.service.ApiKeyService;
import com.mailshop_dragonvu.service.impl.CustomUserDetailsService;
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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_PARAM = "apikey"; // Query parameter name

    private final ApiKeyService apiKeyService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String apiKey = extractApiKeyFromRequest(request);

            if (StringUtils.hasText(apiKey)) {
                UserEntity userEntity = apiKeyService.validateApiKey(apiKey);
                if (userEntity != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEntity.getEmail());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            log.error("API key authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract API key from request - hỗ trợ cả header và query parameter
     * Ưu tiên: Header > Query parameter
     * 
     * Header format: X-API-KEY: msk_xxxx
     * URL format: ?apikey=msk_xxxx
     */
    private String extractApiKeyFromRequest(HttpServletRequest request) {
        // 1. Check header first (priority)
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (StringUtils.hasText(apiKey)) {
            return apiKey.trim();
        }
        
        // 2. Fallback to query parameter
        apiKey = request.getParameter(API_KEY_PARAM);
        return StringUtils.hasText(apiKey) ? apiKey.trim() : null;
    }
}

