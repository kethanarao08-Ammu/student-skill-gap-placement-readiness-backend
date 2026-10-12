package com.studentreadiness.controller;

import com.studentreadiness.dto.AssessmentResultResponse;
import com.studentreadiness.dto.AssessmentSubmitRequest;
import com.studentreadiness.dto.QuestionDisplayDto;
import com.studentreadiness.entity.Assessment;
import com.studentreadiness.entity.AssessmentAttempt;
import com.studentreadiness.security.SecurityUtils;
import com.studentreadiness.service.AssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public ResponseEntity<List<Assessment>> getActiveAssessments() {
        return ResponseEntity.ok(assessmentService.getAllActiveAssessments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assessment> getAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentById(id));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionDisplayDto>> getAssessmentQuestions(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentQuestions(id));
    }

    @PostMapping("/submit")
    public ResponseEntity<AssessmentResultResponse> submitAssessment(@RequestBody AssessmentSubmitRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        AssessmentResultResponse result = assessmentService.submitAssessment(userId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AssessmentAttempt>> getAttemptHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(assessmentService.getAttemptHistory(userId));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<AssessmentResultResponse> getAttemptResult(@PathVariable Long attemptId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(assessmentService.getAttemptResult(userId, attemptId));
    }
}
