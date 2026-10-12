package com.studentreadiness.controller;

import com.studentreadiness.dto.AuthResponse;
import com.studentreadiness.dto.CertificationRequest;
import com.studentreadiness.dto.ProjectRequest;
import com.studentreadiness.dto.RegisterRequest;
import com.studentreadiness.entity.JobRole;
import com.studentreadiness.entity.JobRoleSkill;
import com.studentreadiness.entity.Skill;
import com.studentreadiness.enums.SkillCategory;
import com.studentreadiness.repository.JobRoleRepository;
import com.studentreadiness.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:student-data-${random.uuid};DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class StudentDataEndpointsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private JobRoleRepository jobRoleRepository;

    @Test
    void projectAndCertificationCanBeCreatedForAStudentProfile() {
        HttpHeaders headers = registerStudent();

        ProjectRequest project = new ProjectRequest();
        project.setTitle("Integration Project");
        project.setDescription("Project endpoint integration test");
        project.setTechnologies("Java, Spring");
        ResponseEntity<String> projectResponse = restTemplate.postForEntity(
                url("/projects"), new HttpEntity<>(project, headers), String.class);
        assertEquals(HttpStatus.CREATED, projectResponse.getStatusCode());

        CertificationRequest certification = new CertificationRequest();
        certification.setTitle("Integration Certification");
        certification.setIssuingOrganization("Test Organization");
        certification.setIssueDate(java.time.LocalDate.of(2025, 1, 1));
        ResponseEntity<String> certificationResponse = restTemplate.postForEntity(
                url("/certifications"), new HttpEntity<>(certification, headers), String.class);
        assertEquals(HttpStatus.CREATED, certificationResponse.getStatusCode());
    }

    @Test
    void generatedLearningRecommendationsPersistTheirRequiredLegacyProfileLink() {
        String suffix = UUID.randomUUID().toString();
        Skill skill = skillRepository.saveAndFlush(new Skill(
                null, "Learning Test Skill " + suffix, SkillCategory.BACKEND, "Integration test skill"));
        JobRole role = new JobRole(null, "Learning Test Role " + suffix, "Integration test role", true);
        role.getRequiredSkills().add(new JobRoleSkill(role, skill, 4, 5));
        role = jobRoleRepository.saveAndFlush(role);

        HttpHeaders headers = registerStudent();
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/learning-path/generate?jobRoleId=" + role.getId()),
                new HttpEntity<>(headers),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private HttpHeaders registerStudent() {
        String suffix = UUID.randomUUID().toString();
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Student Data Test");
        request.setEmail("student-data-" + suffix + "@example.test");
        request.setPassword("RuntimePass1!");
        ResponseEntity<AuthResponse> registration = restTemplate.postForEntity(
                url("/auth/register"), request, AuthResponse.class);
        assertEquals(HttpStatus.CREATED, registration.getStatusCode());
        String token = Objects.requireNonNull(registration.getBody()).getAccessToken();
        if (token == null) {
            throw new AssertionError("Registration did not return an access token");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }
}
