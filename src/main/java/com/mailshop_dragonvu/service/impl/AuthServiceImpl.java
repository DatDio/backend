package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.request.LoginRequest;
import com.mailshop_dragonvu.dto.request.RefreshTokenRequest;
import com.mailshop_dragonvu.dto.request.RegisterRequest;
import com.mailshop_dragonvu.dto.response.AuthResponse;
import com.mailshop_dragonvu.dto.response.UserResponse;
import com.mailshop_dragonvu.entity.RefreshToken;
import com.mailshop_dragonvu.entity.Role;
import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.enums.AuthProvider;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.UserMapper;
import com.mailshop_dragonvu.repository.RefreshTokenRepository;
import com.mailshop_dragonvu.repository.RoleRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.security.JwtTokenProvider;
import com.mailshop_dragonvu.service.AuthService;
import com.mailshop_dragonvu.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .build();

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        user.getRoles().add(userRole);

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        // Generate tokens
        String authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.generateTokenFromUserId(
                user.getId(),
                user.getEmail(),
                authorities
        );

        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());

        // Save refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        // Send welcome email asynchronously
        try {
            emailService.sendWelcomeEmail(user);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }

        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());

        // Save refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        UserResponse userResponse = userMapper.toResponse(user);

        log.info("User logged in successfully: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (refreshToken.getRevoked()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID, "Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();

        String authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.generateTokenFromUserId(
                user.getId(),
                user.getEmail(),
                authorities
        );

        UserResponse userResponse = userMapper.toResponse(user);

        log.info("Access token refreshed successfully for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("User logout");

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });

        log.info("User logged out successfully");
    }

}
