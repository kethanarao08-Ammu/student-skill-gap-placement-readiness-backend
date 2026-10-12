package com.studentreadiness.service;

import com.studentreadiness.dto.ReadinessScoreResponse;
import com.studentreadiness.dto.SkillGapResponse;
import com.studentreadiness.entity.AssessmentAttempt;
import com.studentreadiness.enums.ReadinessCategory;
import com.studentreadiness.repository.AssessmentAttemptRepository;
import com.studentreadiness.repository.CertificationRepository;
import com.studentreadiness.repository.StudentProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ReadinessService {

    private final SkillGapService skillGapService;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final StudentProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;

    public ReadinessService(SkillGapService skillGapService,
                            AssessmentAttemptRepository assessmentAttemptRepository,
                            StudentProjectRepository projectRepository,
                            CertificationRepository certificationRepository) {
        this.skillGapService = skillGapService;
        this.assessmentAttemptRepository = assessmentAttemptRepository;
        this.projectRepository = projectRepository;
        this.certificationRepository = certificationRepository;
    }

    @Transactional(readOnly = true)
    public ReadinessScoreResponse calculateReadiness(Long userId, Long jobRoleId) {
        // 1. Skill Gap & Match (40%)
        SkillGapResponse gap = skillGapService.calculateSkillGap(userId, jobRoleId);
        double skillMatchScore = gap.getWeightedMatchPercentage();

        // 2. Assessment Score (35%)
        Optional<AssessmentAttempt> latestAttempt = assessmentAttemptRepository.findTopByUserIdOrderByAttemptedAtDesc(userId);
        double assessmentScore = latestAttempt.map(a -> a.getScorePercentage().doubleValue()).orElse(0.0);
        boolean hasAssessment = latestAttempt.isPresent();

        // 3. Projects Score (15%)
        long projectCount = projectRepository.countByUserId(userId);
        double projectScore;
        if (projectCount == 0) {
            projectScore = 0.0;
        } else if (projectCount == 1) {
            projectScore = 40.0;
        } else if (projectCount == 2) {
            projectScore = 75.0;
        } else {
            projectScore = 100.0;
        }

        // 4. Certifications Score (10%)
        long certCount = certificationRepository.countByUserId(userId);
        double certScore;
        if (certCount == 0) {
            certScore = 0.0;
        } else if (certCount == 1) {
            certScore = 60.0;
        } else {
            certScore = 100.0;
        }

        // Overall Weighted Formula
        // 40% skills + 35% assessment + 15% projects + 10% certs
        double overallScore = (skillMatchScore * 0.40)
                + (assessmentScore * 0.35)
                + (projectScore * 0.15)
                + (certScore * 0.10);
        overallScore = round(overallScore);

        List<String> missing = new ArrayList<>();
        if (!hasAssessment) {
            missing.add("Technical Assessment");
        }
        if (projectCount == 0) {
            missing.add("Practical Projects");
        }
        if (certCount == 0) {
            missing.add("Industry Certifications");
        }
        if (gap.getTotalRequiredSkills() == 0 || skillMatchScore == 0) {
            missing.add("Verified Technical Skills");
        }

        boolean isProvisional = !missing.isEmpty();
        String provisionalReason = isProvisional
                ? "Score is provisional. Pending items: " + String.join(", ", missing)
                : null;

        ReadinessCategory category = ReadinessCategory.fromScore(overallScore);

        ReadinessScoreResponse response = new ReadinessScoreResponse();
        response.setJobRoleId(gap.getJobRoleId());
        response.setJobRoleTitle(gap.getJobRoleTitle());
        response.setOverallReadinessScore(overallScore);
        response.setReadinessCategory(category.getDisplayName());
        response.setProvisional(isProvisional);
        response.setProvisionalReason(provisionalReason);

        response.setSkillMatchScore(skillMatchScore);
        response.setAssessmentScore(assessmentScore);
        response.setProjectScore(projectScore);
        response.setCertificationScore(certScore);

        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put("skillMatch", 0.40);
        weights.put("assessment", 0.35);
        weights.put("projects", 0.15);
        weights.put("certifications", 0.10);
        response.setComponentWeights(weights);

        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("skillMatch", skillMatchScore);
        scores.put("assessment", assessmentScore);
        scores.put("projects", projectScore);
        scores.put("certifications", certScore);
        response.setComponentScores(scores);

        response.setMissingComponents(missing);

        return response;
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
