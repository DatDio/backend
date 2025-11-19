package com.mailshop_dragonvu.service.auth;

import com.mailshop_dragonvu.dto.auth.LoginRequest;
import com.mailshop_dragonvu.dto.auth.RefreshTokenRequest;
import com.mailshop_dragonvu.dto.auth.RegisterRequest;
import com.mailshop_dragonvu.dto.auth.AuthResponse;
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
import com.mailshop_dragonvu.service.EmailService;
import com.mailshop_dragonvu.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final GoogleTokenVerifier googleTokenVerifier;
    private final WalletService walletService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

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

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        user.getRoles().add(userRole);

        user = userRepository.save(user);

        //Tạo ví
         walletService.createWallet(user.getId());

        String authorities = user.getRoles()
                .stream()
                .map(r -> "ROLE_" + r.getName())
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.generateTokenFromUserId(
                user.getId(), user.getEmail(), authorities
        );

        RefreshToken refreshToken = createOrUpdateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toResponse(user))
                .build();
    }


    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtTokenProvider.generateToken(authentication);

        RefreshToken refreshToken = createOrUpdateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toResponse(user))
                .build();
    }


    private RefreshToken createOrUpdateRefreshToken(User user) {
        String newToken = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(RefreshToken.builder()
                        .user(user)
                        .build());

        refreshToken.setToken(newToken);
        refreshToken.setRevoked(false);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(String idToken) {

        var payload = googleTokenVerifier.verify(idToken);
        if (payload == null) {
            throw new BusinessException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        String email = payload.getEmail();
        String fullName = (String) payload.get("name");
        String avatarUrl = (String) payload.get("picture");
        String googleId = payload.getSubject();

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .fullName(fullName)
                            .avatarUrl(avatarUrl)
                            .authProvider(AuthProvider.GOOGLE)
                            .providerId(googleId)
                            .emailVerified(true)
                            .build();

                    Role userRole = roleRepository.findByName("USER")
                            .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
                    newUser.getRoles().add(userRole);

                    return userRepository.save(newUser);
                });

        // ---- JWT + Refresh token ----
        String authorities = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.generateTokenFromUserId(
                user.getId(), user.getEmail(), authorities);

        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .token(refreshTokenStr)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                        .build()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (refreshToken.getRevoked()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();

        String authorities = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.generateTokenFromUserId(
                user.getId(),
                user.getEmail(),
                authorities
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken()) // giữ nguyên
                .user(userMapper.toResponse(user))
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
