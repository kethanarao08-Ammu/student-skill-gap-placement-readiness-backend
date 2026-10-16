package com.studentreadiness.config;

import com.studentreadiness.entity.User;
import com.studentreadiness.entity.StudentProfile;
import com.studentreadiness.enums.Role;
import com.studentreadiness.repository.StudentProfileRepository;
import com.studentreadiness.repository.UserRepository;
import com.studentreadiness.repository.JobRoleSkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final JobRoleSkillRepository jobRoleSkillRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentProfileRepository profileRepository,
                           JobRoleSkillRepository jobRoleSkillRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.jobRoleSkillRepository = jobRoleSkillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("Initializing / verifying default user credentials...");
        int repairedRoleRequirements = jobRoleSkillRepository.restoreRequiredLevelsFromLegacyValues();
        if (repairedRoleRequirements > 0) {
            logger.info("Restored required levels for {} job-role skills from legacy values",
                    repairedRoleRequirements);
        }

        ensureDefaultUser(
                "admin@skillgap.edu",
                "Admin@123",
                "Placement Director Admin",
                Role.ROLE_ADMIN
        );

        ensureDefaultUser(
                "student@skillgap.edu",
                "Student@123",
                "Alex Sharma",
                Role.ROLE_STUDENT
        );
    }

    private void ensureDefaultUser(String email, String rawPassword, String fullName, Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    user.setEnabled(true);
                    if (shouldRefreshDefaultPassword(user, rawPassword)) {
                        user.setPassword(passwordEncoder.encode(rawPassword));
                        logger.info("Reset demo password for {}", email);
                    }
                    userRepository.save(user);
                    if (Role.ROLE_STUDENT.equals(role)) {
                        ensureStudentProfile(user);
                    }
                },
                () -> {
                    User user = new User(email, passwordEncoder.encode(rawPassword), fullName, role);
                    User savedUser = userRepository.save(user);
                    if (Role.ROLE_STUDENT.equals(role)) {
                        ensureStudentProfile(savedUser);
                    }
                    logger.info("Created default {} user {}", role.name(), email);
                }
        );
    }

    private boolean shouldRefreshDefaultPassword(User user, String rawPassword) {
        String encodedPassword = user.getPassword();
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return true;
        }

        if (encodedPassword.equals(rawPassword)) {
            return true;
        }

        return !passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private void ensureStudentProfile(User student) {
        if (profileRepository.findByUserId(student.getId()).isEmpty()) {
            profileRepository.save(new StudentProfile(student));
        }
    }
}
