package com.studentreadiness.config;

import com.studentreadiness.entity.User;
import com.studentreadiness.enums.Role;
import com.studentreadiness.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("Initializing / verifying default user credentials...");

        // Ensure admin account exists with valid BCrypt password
        userRepository.findByEmail("admin@skillgap.edu").ifPresentOrElse(
                admin -> {
                    // Update password to ensure valid BCrypt hash for "Admin@123"
                    admin.setPassword(passwordEncoder.encode("Admin@123"));
                    admin.setEnabled(true);
                    userRepository.save(admin);
                    logger.info("Updated password for admin@skillgap.edu");
                },
                () -> {
                    User admin = new User(
                            "admin@skillgap.edu",
                            passwordEncoder.encode("Admin@123"),
                            "Placement Director Admin",
                            Role.ROLE_ADMIN
                    );
                    userRepository.save(admin);
                    logger.info("Created default admin user admin@skillgap.edu");
                }
        );

        // Ensure student account exists with valid BCrypt password
        userRepository.findByEmail("student@skillgap.edu").ifPresentOrElse(
                student -> {
                    student.setPassword(passwordEncoder.encode("Student@123"));
                    student.setEnabled(true);
                    userRepository.save(student);
                    logger.info("Updated password for student@skillgap.edu");
                },
                () -> {
                    User student = new User(
                            "student@skillgap.edu",
                            passwordEncoder.encode("Student@123"),
                            "Alex Sharma",
                            Role.ROLE_STUDENT
                    );
                    userRepository.save(student);
                    logger.info("Created default student user student@skillgap.edu");
                }
        );
    }
}
