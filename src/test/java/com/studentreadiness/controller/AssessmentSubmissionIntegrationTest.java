package com.studentreadiness.controller;

import com.studentreadiness.dto.AssessmentResultResponse;
import com.studentreadiness.dto.AssessmentSubmitRequest;
import com.studentreadiness.dto.AuthResponse;
import com.studentreadiness.dto.RegisterRequest;
import com.studentreadiness.entity.Assessment;
import com.studentreadiness.entity.AssessmentOption;
import com.studentreadiness.entity.AssessmentQuestion;
import com.studentreadiness.repository.AssessmentOptionRepository;
import com.studentreadiness.repository.AssessmentQuestionRepository;
import com.studentreadiness.repository.AssessmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:assessment-submit-${random.uuid};DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AssessmentSubmissionIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentQuestionRepository questionRepository;

    @Autowired
    private AssessmentOptionRepository optionRepository;

    @Test
    void submittingAnswersReturnsAssessmentResults() {
        String suffix = UUID.randomUUID().toString();
        Assessment assessment = assessmentRepository.saveAndFlush(new Assessment(
                null, "Assessment Test " + suffix, "Integration test assessment",
                "Technical", 1, 15, true));
        AssessmentQuestion question = questionRepository.saveAndFlush(new AssessmentQuestion(
                assessment, "What is the correct answer?", "General", "Test explanation"));
        AssessmentOption correctOption = optionRepository.saveAndFlush(
                new AssessmentOption(question, "A", "Correct", true));
        optionRepository.saveAndFlush(new AssessmentOption(question, "B", "Incorrect", false));

        RegisterRequest registrationRequest = new RegisterRequest();
        registrationRequest.setFullName("Assessment Test");
        registrationRequest.setEmail("assessment-" + suffix + "@example.test");
        registrationRequest.setPassword("RuntimePass1!");
        ResponseEntity<AuthResponse> registration = restTemplate.postForEntity(
                url("/auth/register"), registrationRequest, AuthResponse.class);
        assertEquals(HttpStatus.CREATED, registration.getStatusCode());
        AuthResponse registrationBody = Objects.requireNonNull(registration.getBody());
        String accessToken = registrationBody.getAccessToken();
        if (accessToken == null) {
            throw new AssertionError("Registration did not return an access token");
        }

        AssessmentSubmitRequest request = new AssessmentSubmitRequest();
        request.setAssessmentId(assessment.getId());
        request.setAnswers(Map.of(question.getId(), correctOption.getId()));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<AssessmentResultResponse> response = restTemplate.postForEntity(
                url("/assessments/submit"),
                new HttpEntity<>(request, headers),
                AssessmentResultResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AssessmentResultResponse result = Objects.requireNonNull(response.getBody());
        assertEquals(1, result.getTotalQuestions());
        assertEquals(1, result.getCorrectAnswers());
        assertEquals(100.0, result.getScorePercentage());
    }

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }
}
