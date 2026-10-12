package com.studentreadiness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentreadiness.dto.AuthResponse;
import com.studentreadiness.dto.RegisterRequest;
import com.studentreadiness.entity.StudentProfile;
import com.studentreadiness.entity.User;
import com.studentreadiness.exception.DuplicateResourceException;
import com.studentreadiness.repository.StudentProfileRepository;
import com.studentreadiness.repository.UserRepository;
import com.studentreadiness.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentProfileRepository profileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerMapsJsonPersistsUserAndStudentProfile() throws Exception {
        RegisterRequest request = new ObjectMapper().readValue("""
                {
                  "fullName": "Ava Student",
                  "email": " Ava@Example.edu ",
                  "password": "Password1!",
                  "college": " Example University ",
                  "degree": "B.Tech",
                  "branch": "Computer Science",
                  "graduationYear": 2027
                }
                """, RegisterRequest.class);
        Authentication authentication = mock(Authentication.class);

        when(userRepository.existsByEmail("ava@example.edu")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("signed-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("Ava Student", savedUser.getFullName());
        assertEquals("ava@example.edu", savedUser.getEmail());
        assertEquals("bcrypt-hash", savedUser.getPassword());

        ArgumentCaptor<StudentProfile> profileCaptor = ArgumentCaptor.forClass(StudentProfile.class);
        verify(profileRepository).save(profileCaptor.capture());
        StudentProfile profile = profileCaptor.getValue();
        assertSame(savedUser, profile.getUser());
        assertEquals("Example University", profile.getCollege());
        assertEquals("B.Tech", profile.getDegree());
        assertEquals("Computer Science", profile.getBranch());
        assertEquals(2027, profile.getGraduationYear());
        assertEquals(42L, response.getId());
        assertEquals("signed-token", response.getAccessToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerAcceptsDatabaseStyleFullNameProperty() throws Exception {
        RegisterRequest request = new ObjectMapper().readValue(
                "{\"full_name\":\"Ava Student\",\"email\":\"ava@example.edu\",\"password\":\"Password1!\"}",
                RegisterRequest.class);

        assertEquals("Ava Student", request.getFullName());
    }

    @Test
    void registerRejectsDuplicateEmailBeforeSaving() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ava Student");
        request.setEmail(" Ava@Example.edu ");
        request.setPassword("Password1!");
        when(userRepository.existsByEmail("ava@example.edu")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
        verify(profileRepository, never()).save(any(StudentProfile.class));
    }
}