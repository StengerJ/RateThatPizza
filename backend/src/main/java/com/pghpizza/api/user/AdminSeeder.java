package com.pghpizza.api.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pghpizza.api.common.TextSanitizer;
import com.pghpizza.api.config.AppProperties;

@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, AppProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = TextSanitizer.normalizeEmail(properties.admin().email());
        UserEntity admin = userRepository.findByEmail(email).orElseGet(UserEntity::new);
        admin.setEmail(email);
        admin.setDisplayName(TextSanitizer.trim(properties.admin().displayName()));
        admin.setPasswordHash(passwordEncoder.encode(properties.admin().password()));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);

        if ("admin@pgh-pizza.local".equals(email) && "ChangeMe123!".equals(properties.admin().password())) {
            log.warn("Using default local admin credentials. Override PGH_ADMIN_EMAIL and PGH_ADMIN_PASSWORD outside local development.");
        }
    }
}
