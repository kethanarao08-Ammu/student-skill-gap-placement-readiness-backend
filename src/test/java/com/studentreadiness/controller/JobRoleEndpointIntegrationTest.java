package com.studentreadiness.controller;

import com.studentreadiness.dto.AuthResponse;
import com.studentreadiness.dto.RegisterRequest;
import com.studentreadiness.entity.JobRole;
import com.studentreadiness.entity.JobRoleSkill;
import com.studentreadiness.entity.Skill;
import com.studentreadiness.enums.SkillCategory;
import com.studentreadiness.repository.JobRoleRepository;
import com.studentreadiness.repository.JobRoleSkillRepository;
import com.studentreadiness.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:job-role-${random.uuid};DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class JobRoleEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private JobRoleRepository jobRoleRepository;

    @Autowired
    private JobRoleSkillRepository jobRoleSkillRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void activeRolesIncludeTheirRequiredSkillsInHttpResponse() {
        String email = "job-role-" + UUID.randomUUID() + "@example.test";
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Job Role Test");
        request.setEmail(email);
        request.setPassword("RuntimePass1!");

        ResponseEntity<AuthResponse> registration = restTemplate.postForEntity(
                url("/auth/register"), request, AuthResponse.class);
        assertEquals(HttpStatus.CREATED, registration.getStatusCode());
        AuthResponse registrationBody = Objects.requireNonNull(registration.getBody());
        assertNotNull(registrationBody);
        String accessToken = registrationBody.getAccessToken();
        if (accessToken == null) {
            throw new AssertionError("Registration did not return an access token");
        }

        Skill skill = new Skill(null, "Test Skill " + UUID.randomUUID(),
                SkillCategory.BACKEND, "Integration test skill");
        skill = skillRepository.saveAndFlush(skill);

        JobRole role = new JobRole(null, "Test Role " + UUID.randomUUID(),
                "Integration test role", true);
        role.getRequiredSkills().add(new JobRoleSkill(role, skill, 4, 5));
        jobRoleRepository.saveAndFlush(role);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url("/job-roles"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() { }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Map<String, Object>> responseBody = Objects.requireNonNull(response.getBody());
        Map<String, Object> savedRole = responseBody.stream()
                .filter(item -> role.getTitle().equals(item.get("title")))
                .findFirst()
                .orElseThrow();
        List<?> requiredSkills = (List<?>) savedRole.get("requiredSkills");
        assertEquals(1, requiredSkills.size());
        Object roleSkillValue = requiredSkills.get(0);
        if (!(roleSkillValue instanceof Map<?, ?> roleSkill)) {
            throw new AssertionError("Required skill was not serialized as an object");
        }
        Object nestedSkillValue = roleSkill.get("skill");
        if (!(nestedSkillValue instanceof Map<?, ?> nestedSkill)) {
            throw new AssertionError("Skill was not serialized as an object");
        }
        assertEquals(skill.getName(), nestedSkill.get("name"));
    }

    @Test
    void restoresLegacyRequiredProficiencyAndReportsTheSkillAsMissing() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Legacy Requirement Test");
        request.setEmail("legacy-requirement-" + UUID.randomUUID() + "@example.test");
        request.setPassword("RuntimePass1!");
        ResponseEntity<AuthResponse> registration = restTemplate.postForEntity(
                url("/auth/register"), request, AuthResponse.class);
        assertEquals(HttpStatus.CREATED, registration.getStatusCode());
        String token = Objects.requireNonNull(registration.getBody()).getAccessToken();

        Skill skill = skillRepository.saveAndFlush(new Skill(
                null, "Legacy Skill " + UUID.randomUUID(), SkillCategory.BACKEND, "Legacy value test"));
        JobRole role = new JobRole(null, "Legacy Role " + UUID.randomUUID(), "Legacy role test", true);
        role.getRequiredSkills().add(new JobRoleSkill(role, skill, 4, 5));
        role = jobRoleRepository.saveAndFlush(role);
        Long roleSkillId = jobRoleSkillRepository.findByJobRoleId(role.getId()).get(0).getId();

        jdbcTemplate.update(
                "UPDATE job_role_skills SET required_level = 0, required_proficiency = 4 WHERE id = ?",
                roleSkillId);
        assertEquals(1, jobRoleSkillRepository.restoreRequiredLevelsFromLegacyValues());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(Objects.requireNonNull(token));
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url("/skill-gap/analyze?jobRoleId=" + role.getId()),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() { }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = Objects.requireNonNull(response.getBody());
        assertEquals(1, responseBody.get("missingSkillsCount"));
        List<?> missingSkills = (List<?>) responseBody.get("missingSkills");
        Map<?, ?> missingSkill = (Map<?, ?>) missingSkills.get(0);
        assertEquals(4, missingSkill.get("requiredLevel"));
        assertEquals(4, missingSkill.get("gap"));
    }

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }
}
