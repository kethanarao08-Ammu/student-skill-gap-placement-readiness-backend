package com.studentreadiness.controller;

import com.studentreadiness.dto.AuthResponse;
import com.studentreadiness.dto.RegisterRequest;
import com.studentreadiness.dto.StudentSkillRequest;
import com.studentreadiness.dto.StudentSkillResponse;
import com.studentreadiness.entity.Skill;
import com.studentreadiness.enums.SkillCategory;
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
        "spring.datasource.url=jdbc:h2:mem:student-skill-${random.uuid};DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class StudentSkillEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SkillRepository skillRepository;

    @Test
    void savingStudentSkillPersistsTheProfileAndUserRelationships() {
        String suffix = UUID.randomUUID().toString();
        RegisterRequest registrationRequest = new RegisterRequest();
        registrationRequest.setFullName("Student Skill Test");
        registrationRequest.setEmail("student-skill-" + suffix + "@example.test");
        registrationRequest.setPassword("RuntimePass1!");
        ResponseEntity<AuthResponse> registration = restTemplate.postForEntity(
                url("/auth/register"), registrationRequest, AuthResponse.class);
        assertEquals(HttpStatus.CREATED, registration.getStatusCode());
        String accessToken = Objects.requireNonNull(registration.getBody()).getAccessToken();
        if (accessToken == null) {
            throw new AssertionError("Registration did not return an access token");
        }

        Skill skill = skillRepository.saveAndFlush(new Skill(
                null, "Student Skill Test " + suffix, SkillCategory.BACKEND, "Integration test skill"));
        StudentSkillRequest request = new StudentSkillRequest();
        request.setSkillId(skill.getId());
        request.setProficiencyLevel(4);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<StudentSkillResponse> response = restTemplate.postForEntity(
                url("/skills/student"), new HttpEntity<>(request, headers), StudentSkillResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        StudentSkillResponse savedSkill = Objects.requireNonNull(response.getBody());
        assertEquals(skill.getId(), savedSkill.getSkillId());
        assertEquals(4, savedSkill.getProficiencyLevel());
    }

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }
}
