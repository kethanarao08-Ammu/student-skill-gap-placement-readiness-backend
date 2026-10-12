package com.studentreadiness.service;

import com.studentreadiness.dto.AuthResponse;
import com.studentreadiness.dto.LoginRequest;
import com.studentreadiness.dto.RegisterRequest;
import com.studentreadiness.entity.StudentProfile;
import com.studentreadiness.entity.User;
import com.studentreadiness.enums.Role;
import com.studentreadiness.exception.DuplicateResourceException;
import com.studentreadiness.repository.StudentProfileRepository;
import com.studentreadiness.repository.UserRepository;
import com.studentreadiness.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       StudentProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with email '" + request.getEmail() + "' already exists.");
        }

        User user = new User();
        user.setEmail(email);
        user.setFullName(request.getFullName().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_STUDENT);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        StudentProfile profile = new StudentProfile(savedUser);
        profile.setCollege(trimToNull(request.getCollege()));
        profile.setDegree(trimToNull(request.getDegree()));
        profile.setBranch(trimToNull(request.getBranch()));
        profile.setGraduationYear(request.getGraduationYear());
        profileRepository.save(profile);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);

        AuthResponse response = new AuthResponse(token, savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole().name());
        response.setId(savedUser.getId());
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow();

        AuthResponse response = new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
        response.setId(user.getId());
        return response;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
