package com.studentreadiness.config;

import com.studentreadiness.entity.User;
import com.studentreadiness.enums.Role;
import com.studentreadiness.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Profile("render")
public class RenderAdminInitializer implements CommandLineRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(RenderAdminInitializer.class);
    private static final int MINIMUM_PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String configuredEmail;
    private final String configuredPassword;

    public RenderAdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.bootstrap.email:}") String configuredEmail,
            @Value("${app.admin.bootstrap.password:}") String configuredPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.configuredEmail = configuredEmail;
        this.configuredPassword = configuredPassword;
    }

    @Override
    public void run(String... args) {
        boolean hasEmail = configuredEmail != null && !configuredEmail.isBlank();
        boolean hasPassword = configuredPassword != null && !configuredPassword.isBlank();

        if (!hasEmail && !hasPassword) {
            logger.info("Admin bootstrap is not configured; no Render admin account was changed");
            return;
        }
        if (!hasEmail || !hasPassword) {
            throw new IllegalStateException(
                    "Set both ADMIN_BOOTSTRAP_EMAIL and ADMIN_BOOTSTRAP_PASSWORD to bootstrap a Render admin account");
        }
        if (configuredPassword.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "ADMIN_BOOTSTRAP_PASSWORD must be at least 12 characters long");
        }

        String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalStateException(
                    "ADMIN_BOOTSTRAP_EMAIL must be a valid email address");
        }

        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    if (user.getRole() != Role.ROLE_ADMIN) {
                        throw new IllegalStateException(
                                "The configured Render admin email already belongs to a non-admin account; no account was changed");
                    }
                    logger.info("Configured Render admin account already exists; it was not changed");
                },
                () -> {
                    User admin = new User(
                            email,
                            passwordEncoder.encode(configuredPassword),
                            "Placement Administrator",
                            Role.ROLE_ADMIN);
                    userRepository.save(admin);
                    logger.info("Created configured Render admin account");
                }
        );
    }
}
