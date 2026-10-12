package com.studentreadiness.controller;

import com.studentreadiness.dto.LearningProgressUpdateRequest;
import com.studentreadiness.dto.LearningRecommendationResponse;
import com.studentreadiness.enums.LearningStatus;
import com.studentreadiness.security.SecurityUtils;
import com.studentreadiness.service.LearningPathService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/learning-path")
public class LearningPathController {

    private final LearningPathService learningPathService;

    public LearningPathController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<LearningRecommendationResponse>> getRecommendations() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(learningPathService.getRecommendations(userId));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<LearningRecommendationResponse>> generateRecommendations(
            @RequestParam(required = false) Long jobRoleId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<LearningRecommendationResponse> recommendations = learningPathService.generateRecommendations(userId, jobRoleId);
        return ResponseEntity.ok(recommendations);
    }

    @PatchMapping("/recommendations/{id}/status")
    public ResponseEntity<LearningRecommendationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody LearningProgressUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        LearningStatus status = LearningStatus.valueOf(request.getStatus().toUpperCase());
        LearningRecommendationResponse response = learningPathService.updateStatus(userId, id, status);
        return ResponseEntity.ok(response);
    }
}
