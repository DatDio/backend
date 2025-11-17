package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.entity.Role;
import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.enums.AuthProvider;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.repository.RoleRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user: " + ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        if (email == null || email.isEmpty()) {
            throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED, "Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getAuthProvider().equals(authProvider)) {
                throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_FAILED,
                        "Email already registered with " + user.getAuthProvider() + " provider");
            }
            user = updateExistingUser(user, name);
        } else {
            user = registerNewUser(email, name, providerId, authProvider);
        }

        return oAuth2User;
    }

    private User registerNewUser(String email, String name, String providerId, AuthProvider authProvider) {
        log.info("Registering new OAuth2 user: {}", email);

        User user = User.builder()
                .email(email)
                .fullName(name)
                .authProvider(authProvider)
                .providerId(providerId)
                .emailVerified(true)
                .build();

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        user.getRoles().add(userRole);

        return userRepository.save(user);
    }

    private User updateExistingUser(User user, String name) {
        log.info("Updating existing OAuth2 user: {}", user.getEmail());

        if (name != null && !name.equals(user.getFullName())) {
            user.setFullName(name);
            user = userRepository.save(user);
        }

        return user;
    }

}
