package com.mailshop_dragonvu.config;

import com.mailshop_dragonvu.entity.RoleEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.ActiveStatusEnum;
import com.mailshop_dragonvu.enums.AuthProvider;
import com.mailshop_dragonvu.repository.RoleRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-email}")
    private String adminEmail;

    @Value("${app.admin.default-password}")
    private String adminPassword;

    private static final List<String> DEFAULT_ROLES = List.of("USER", "ADMIN");

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== [START] DATA SEEDING ===");

        seedRoles();
        seedAdmin();

        log.info("=== [DONE] DATA SEEDING ===");
    }

    /**
     * Seed all required roles if missing
     */
    private void seedRoles() {
        log.info("[ROLE] Checking & seeding roles...");

        for (String roleName : DEFAULT_ROLES) {
            roleRepository.findByName(roleName)
                    .or(() -> Optional.of(roleRepository.save(
                            RoleEntity.builder()
                                    .name(roleName)
                                    .description(roleName.equals("ADMIN") ?
                                            "Administrator with full system access" :
                                            "Standard user with basic access")
                                    .build()
                    )))
                    .ifPresent(role -> log.info("[ROLE] OK => {}", role.getName()));
        }
    }

    /**
     * Seed system admin account if missing
     */
    private void seedAdmin() {
        log.info("[ADMIN] Checking admin user...");

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("[ADMIN] Already exists, skip");
            return;
        }

        RoleEntity adminRoleEntity = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("[ADMIN] ADMIN role missing!"));

        UserEntity admin = UserEntity.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .fullName("System Administrator")
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .roles(Set.of(adminRoleEntity))
                .status(ActiveStatusEnum.ACTIVE)
                .build();

        userRepository.save(admin);
        log.info("[ADMIN] Created admin account: {}", adminEmail);
    }
}
