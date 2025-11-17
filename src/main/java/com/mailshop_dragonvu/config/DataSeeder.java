package com.mailshop_dragonvu.config;

import com.mailshop_dragonvu.entity.Role;
import com.mailshop_dragonvu.entity.User;
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

import java.util.Arrays;
import java.util.Set;

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

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data seeding...");

        seedRoles();
        seedAdmin();

        log.info("Data seeding completed successfully");
    }

    private void seedRoles() {
        if (roleRepository.count() > 0) {
            log.info("Roles already exist, skipping seeding");
            return;
        }

        log.info("Seeding roles...");

        // Create USER role
        Role userRole = Role.builder()
                .name("USER")
                .description("Standard user with basic access")
                .build();

        // Create ADMIN role
        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Administrator with full system access")
                .build();

        roleRepository.saveAll(Arrays.asList(userRole, adminRole));
        log.info("Roles seeded successfully: USER and ADMIN roles created");
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists, skipping seeding");
            return;
        }

        log.info("Seeding admin user...");

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .fullName("System Administrator")
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Admin user seeded successfully with email: {}", adminEmail);
    }

}
