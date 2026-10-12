package com.studentreadiness.controller;

import com.studentreadiness.dto.AuthResponse;
import com.studentreadiness.dto.RegisterRequest;
import com.studentreadiness.entity.JobRole;
import com.studentreadiness.entity.JobRoleSkill;
import com.studentreadiness.entity.Skill;
import com.studentreadiness.enums.SkillCategory;
import com.studentreadiness.repository.JobRoleRepository;
import com.studentreadiness.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:learning-progress-${random.uuid};DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class LearningProgressEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private JobRoleRepository jobRoleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void markingRecommendationDonePersistsCompletionAndRequiresAuthentication() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Skill skill = skillRepository.saveAndFlush(new Skill(
                null, "Progress Test Skill " + suffix, SkillCategory.BACKEND, "Learning progress test"));
        JobRole role = new JobRole(null, "Progress Test Role " + suffix, "Learning progress test", true);
        role.getRequiredSkills().add(new JobRoleSkill(role, skill, 4, 5));
        role = jobRoleRepository.saveAndFlush(role);

        ResponseEntity<String> unauthorizedResponse = restTemplate.getForEntity(
                url("/learning-path/recommendations"), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResponse.getStatusCode());

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Learning Progress Test");
        request.setEmail("learning-progress-" + suffix + "@example.test");
        request.setPassword("RuntimePass1!");
        ResponseEntity<AuthResponse> registration = restTemplate.postForEntity(
                url("/auth/register"), request, AuthResponse.class);
        assertEquals(HttpStatus.CREATED, registration.getStatusCode());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(Objects.requireNonNull(registration.getBody()).getAccessToken());
        ResponseEntity<List<Map<String, Object>>> generated = restTemplate.exchange(
                url("/learning-path/generate?jobRoleId=" + role.getId()),
                HttpMethod.POST,
                new HttpEntity<>(Map.of(), headers),
                new ParameterizedTypeReference<>() { }
        );
        assertEquals(HttpStatus.OK, generated.getStatusCode());
        Map<String, Object> recommendation = Objects.requireNonNull(generated.getBody()).get(0);
        Long recommendationId = ((Number) recommendation.get("id")).longValue();
        assertEquals("NOT_STARTED", recommendation.get("status"));

        int updateStatus = mockMvc.perform(patch(
                        "/learning-path/recommendations/" + recommendationId + "/status")
                .header(HttpHeaders.AUTHORIZATION, headers.getFirst(HttpHeaders.AUTHORIZATION))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
                .andReturn()
                .getResponse()
                .getStatus();
        assertEquals(HttpStatus.OK.value(), updateStatus);

        ResponseEntity<List<Map<String, Object>>> saved = restTemplate.exchange(
                url("/learning-path/recommendations"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() { }
        );
        assertEquals(HttpStatus.OK, saved.getStatusCode());
        assertEquals("COMPLETED", Objects.requireNonNull(saved.getBody()).get(0).get("status"));
    }

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }
}
