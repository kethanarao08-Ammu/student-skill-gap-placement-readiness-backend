package com.studentreadiness.controller;

import com.studentreadiness.dto.ReadinessScoreResponse;
import com.studentreadiness.dto.SkillGapResponse;
import com.studentreadiness.security.SecurityUtils;
import com.studentreadiness.service.ReadinessService;
import com.studentreadiness.service.SkillGapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skill-gap")
public class SkillGapController {

    private final SkillGapService skillGapService;
    private final ReadinessService readinessService;

    public SkillGapController(SkillGapService skillGapService, ReadinessService readinessService) {
        this.skillGapService = skillGapService;
        this.readinessService = readinessService;
    }

    @GetMapping("/analyze")
    public ResponseEntity<SkillGapResponse> analyzeSkillGap(
            @RequestParam(required = false) Long jobRoleId) {
        Long userId = SecurityUtils.getCurrentUserId();
        SkillGapResponse response = skillGapService.calculateSkillGap(userId, jobRoleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/readiness")
    public ResponseEntity<ReadinessScoreResponse> getReadinessScore(
            @RequestParam(required = false) Long jobRoleId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReadinessScoreResponse response = readinessService.calculateReadiness(userId, jobRoleId);
        return ResponseEntity.ok(response);
    }
}
