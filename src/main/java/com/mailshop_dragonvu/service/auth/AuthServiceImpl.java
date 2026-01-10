package com.mailshop_dragonvu.service.auth;

import com.mailshop_dragonvu.dto.auth.*;
import com.mailshop_dragonvu.entity.RefreshTokenEntity;
import com.mailshop_dragonvu.entity.RoleEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.AuthProvider;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.UserMapper;
import com.mailshop_dragonvu.repository.RefreshTokenRepository;
import com.mailshop_dragonvu.repository.RoleRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.security.JwtTokenProvider;
import com.mailshop_dragonvu.service.ApiKeyService;
import com.mailshop_dragonvu.service.EmailService;
import com.mailshop_dragonvu.service.WalletService;
import com.mailshop_dragonvu.utils.Constants;
import com.mailshop_dragonvu.utils.SecurityUtils;
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

        UserEntity userEntity = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .build();

        RoleEntity userRoleEntity = roleRepository.findByName(Constants.ROLE_USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        userEntity.getRoles().add(userRoleEntity);

        userEntity = userRepository.save(userEntity);

        //Tạo ví
         walletService.createWallet(userEntity.getId());

        String authorities = userEntity.getRoles()
                .stream()
                .map(r -> "ROLE_" + r.getName())
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.generateTokenFromUserId(
                userEntity.getId(), userEntity.getEmail(), authorities
        );

        RefreshTokenEntity refreshTokenEntity = createOrUpdateRefreshToken(userEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenEntity.getToken())
                .user(userMapper.toResponse(userEntity))
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

        UserEntity userEntity = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtTokenProvider.generateToken(authentication);

        RefreshTokenEntity refreshTokenEntity = createOrUpdateRefreshToken(userEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenEntity.getToken())
                .user(userMapper.toResponse(userEntity))
                .build();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        UserEntity currentUser = userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        // Không cho trùng mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BusinessException(ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT);
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        // Thu hồi toàn bộ refresh token => buộc đăng nhập lại
        refreshTokenRepository.revokeAllByUser(currentUser);

        log.info("Password changed for user {}", currentUser.getEmail());
    }



    private RefreshTokenEntity createOrUpdateRefreshToken(UserEntity userEntity) {
        String newToken = jwtTokenProvider.generateRefreshToken(userEntity.getId());

        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByUser(userEntity)
                .orElse(RefreshTokenEntity.builder()
                        .user(userEntity)
                        .build());

        refreshTokenEntity.setToken(newToken);
        refreshTokenEntity.setRevoked(false);
        refreshTokenEntity.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));

        return refreshTokenRepository.save(refreshTokenEntity);
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
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUserEntity = UserEntity.builder()
                            .email(email)
                            .fullName(fullName)
                            .avatarUrl(avatarUrl)
                            .authProvider(AuthProvider.GOOGLE)
                            .providerId(googleId)
                            .emailVerified(true)
                            .build();

                    RoleEntity userRoleEntity = roleRepository.findByName(Constants.ROLE_USER)
                            .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
                    newUserEntity.getRoles().add(userRoleEntity);

                    return userRepository.save(newUserEntity);
                });

        //Tạo ví
        walletService.createWallet(userEntity.getId());

        // ---- JWT + Refresh token ----
        String authorities = userEntity.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.generateTokenFromUserId(
                userEntity.getId(), userEntity.getEmail(), authorities);

        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(userEntity.getId());
        refreshTokenRepository.save(
                RefreshTokenEntity.builder()
                        .token(refreshTokenStr)
                        .user(userEntity)
                        .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                        .build()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .user(userMapper.toResponse(userEntity))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (refreshTokenEntity.getRevoked()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        if (refreshTokenEntity.isExpired()) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        UserEntity userEntity = refreshTokenEntity.getUser();

        String authorities = userEntity.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.generateTokenFromUserId(
                userEntity.getId(),
                userEntity.getEmail(),
                authorities
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenEntity.getToken()) // giữ nguyên
                .user(userMapper.toResponse(userEntity))
                .build();
    }


    @Override
    @Transactional
    public void logout(String refreshToken) {

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });

        log.info("User logged out successfully");
    }

}
