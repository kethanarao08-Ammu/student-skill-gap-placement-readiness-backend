package com.studentreadiness.service;

import com.studentreadiness.dto.AuthResponse;
import com.studentreadiness.dto.RegisterRequest;
import com.studentreadiness.entity.StudentProfile;
import com.studentreadiness.repository.StudentProfileRepository;
import com.studentreadiness.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:registration-test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AuthRegistrationIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository profileRepository;

    @Test
    void registrationCreatesUserProfileAndAccessToken() {
        String email = "signup-" + UUID.randomUUID() + "@example.edu";
        RegisterRequest request = new RegisterRequest();
        request.setFullName("New Student");
        request.setEmail(email);
        request.setPassword("StrongPass1!");
        request.setCollege("Example University");
        request.setDegree("B.Tech");
        request.setBranch("Computer Science");
        request.setGraduationYear(2027);

        AuthResponse response = authService.register(request);

        assertNotNull(response.getAccessToken());
        assertEquals(email, response.getEmail());
        assertTrue(userRepository.existsByEmail(email));
        StudentProfile profile = profileRepository.findByUserId(response.getId()).orElseThrow();
        assertEquals("New Student", profile.getFullName());
        assertEquals("Example University", profile.getCollege());
        assertEquals("B.Tech", profile.getDegree());
    }
}
