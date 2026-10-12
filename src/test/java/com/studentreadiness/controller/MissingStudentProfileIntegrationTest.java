package com.studentreadiness.controller;

import com.studentreadiness.dto.AuthResponse;
import com.studentreadiness.dto.RegisterRequest;
import com.studentreadiness.entity.JobRole;
import com.studentreadiness.entity.StudentProfile;
import com.studentreadiness.repository.JobRoleRepository;
import com.studentreadiness.repository.StudentProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:missing-profile-${random.uuid};DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MissingStudentProfileIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentProfileRepository profileRepository;

    @Autowired
    private JobRoleRepository jobRoleRepository;

    @Test
    void profileAndDashboardRequestsRecoverWhenAnExistingStudentHasNoProfile() {
        jobRoleRepository.saveAndFlush(new JobRole(
                null, "Missing Profile Test Role", "Role for the dashboard integration test", true));

        String suffix = UUID.randomUUID().toString();
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Missing Profile Test");
        request.setEmail("missing-profile-" + suffix + "@example.test");
        request.setPassword("RuntimePass1!");

        ResponseEntity<AuthResponse> registration = restTemplate.postForEntity(
                url("/auth/register"), request, AuthResponse.class);
        assertEquals(HttpStatus.CREATED, registration.getStatusCode());
        AuthResponse auth = Objects.requireNonNull(registration.getBody());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(Objects.requireNonNull(auth.getAccessToken()));

        StudentProfile profile = profileRepository.findByUserId(auth.getId()).orElseThrow();
        profileRepository.delete(profile);
        profileRepository.flush();

        ResponseEntity<String> profileResponse = restTemplate.exchange(
                url("/student/profile"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, profileResponse.getStatusCode());
        assertTrue(profileRepository.findByUserId(auth.getId()).isPresent());

        profileRepository.delete(profileRepository.findByUserId(auth.getId()).orElseThrow());
        profileRepository.flush();
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(
                url("/dashboard/summary"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, dashboardResponse.getStatusCode());
        assertTrue(profileRepository.findByUserId(auth.getId()).isPresent());
    }

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }
}
