package com.studentreadiness.config;

import com.studentreadiness.entity.User;
import com.studentreadiness.enums.Role;
import com.studentreadiness.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenderAdminInitializerSafetyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void doesNotPromoteOrModifyAnExistingStudentAccount() {
        User existingStudent = new User(
                1L,
                "existing@example.test",
                "existing-hash",
                "Existing Student",
                Role.ROLE_STUDENT,
                true);
        when(userRepository.findByEmail("existing@example.test"))
                .thenReturn(Optional.of(existingStudent));

        RenderAdminInitializer initializer = new RenderAdminInitializer(
                userRepository,
                passwordEncoder,
                "existing@example.test",
                "secure-bootstrap-password");

        assertThrows(IllegalStateException.class, () -> initializer.run());
        verify(userRepository, never()).save(existingStudent);
        verify(passwordEncoder, never()).encode("secure-bootstrap-password");
    }
}
