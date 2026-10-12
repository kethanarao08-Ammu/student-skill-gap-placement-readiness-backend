package com.studentreadiness.service;

import com.studentreadiness.dto.DashboardSummaryResponse;
import com.studentreadiness.dto.LearningRecommendationResponse;
import com.studentreadiness.dto.ReadinessScoreResponse;
import com.studentreadiness.dto.SkillGapResponse;
import com.studentreadiness.dto.StudentSkillResponse;
import com.studentreadiness.entity.*;
import com.studentreadiness.enums.LearningStatus;
import com.studentreadiness.exception.ResourceNotFoundException;
import com.studentreadiness.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final StudentService studentService;
    private final SkillService skillService;
    private final SkillGapService skillGapService;
    private final ReadinessService readinessService;
    private final AssessmentAttemptRepository attemptRepository;
    private final StudentProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;
    private final LearningRecommendationRepository recommendationRepository;
    private final LearningPathService learningPathService;

    public DashboardService(UserRepository userRepository,
                            StudentProfileRepository profileRepository,
                            StudentService studentService,
                            SkillService skillService,
                            SkillGapService skillGapService,
                            ReadinessService readinessService,
                            AssessmentAttemptRepository attemptRepository,
                            StudentProjectRepository projectRepository,
                            CertificationRepository certificationRepository,
                            LearningRecommendationRepository recommendationRepository,
                            LearningPathService learningPathService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.studentService = studentService;
        this.skillService = skillService;
        this.skillGapService = skillGapService;
        this.readinessService = readinessService;
        this.attemptRepository = attemptRepository;
        this.projectRepository = projectRepository;
        this.certificationRepository = certificationRepository;
        this.recommendationRepository = recommendationRepository;
        this.learningPathService = learningPathService;
    }

    @Transactional
    public DashboardSummaryResponse getDashboardSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        studentService.getProfile(userId);
        StudentProfile profile = profileRepository.findByUserId(userId).orElse(null);
        JobRole targetRole = profile != null ? profile.getTargetJobRole() : null;

        DashboardSummaryResponse summary = new DashboardSummaryResponse();
        summary.setUserId(user.getId());
        summary.setFullName(user.getFullName());
        summary.setEmail(user.getEmail());

        // 1. Profile completion
        summary.setProfileCompletionPercentage(studentService.calculateProfileCompletion(userId));

        // 2. Target Job Role
        if (targetRole != null) {
            summary.setTargetJobRoleId(targetRole.getId());
            summary.setTargetJobRoleTitle(targetRole.getTitle());
        }

        // 3. Skill Gap & Readiness
        SkillGapResponse gap = skillGapService.calculateSkillGap(userId, targetRole != null ? targetRole.getId() : null);
        summary.setSkillMatchPercentage(gap.getWeightedMatchPercentage());

        ReadinessScoreResponse readiness = readinessService.calculateReadiness(userId, targetRole != null ? targetRole.getId() : null);
        summary.setOverallReadinessScore(readiness.getOverallReadinessScore());
        summary.setReadinessCategory(readiness.getReadinessCategory());

        List<Map<String, Object>> topGaps = gap.getHighPriorityGaps().stream()
                .limit(3)
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("skillName", item.getSkillName());
                    map.put("category", item.getSkillCategory());
                    map.put("currentLevel", item.getCurrentLevel());
                    map.put("requiredLevel", item.getRequiredLevel());
                    map.put("gap", item.getGap());
                    map.put("importanceWeight", item.getImportanceWeight());
                    return map;
                })
                .collect(Collectors.toList());
        summary.setTopSkillGaps(topGaps);

        // 4. Assessment History
        Optional<AssessmentAttempt> latestAttempt = attemptRepository.findTopByUserIdOrderByAttemptedAtDesc(userId);
        if (latestAttempt.isPresent()) {
            AssessmentAttempt att = latestAttempt.get();
            summary.setHasCompletedAssessment(true);
            summary.setLatestAssessmentScore(att.getScorePercentage().doubleValue());
            summary.setLatestAssessmentCategory(att.getReadinessCategory());
        } else {
            summary.setHasCompletedAssessment(false);
            summary.setLatestAssessmentScore(0.0);
            summary.setLatestAssessmentCategory("Not Attempted");
        }

        List<AssessmentAttempt> recentAttempts = attemptRepository.findByUserIdOrderByAttemptedAtDesc(userId);
        List<Map<String, Object>> recentAssessments = recentAttempts.stream()
                .limit(3)
                .map(att -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", att.getId());
                    map.put("assessmentTitle", att.getAssessment().getTitle());
                    map.put("scorePercentage", att.getScorePercentage().doubleValue());
                    map.put("readinessCategory", att.getReadinessCategory());
                    map.put("attemptedAt", att.getAttemptedAt());
                    return map;
                })
                .collect(Collectors.toList());
        summary.setRecentAssessments(recentAssessments);

        // 5. Counts
        List<StudentSkillResponse> studentSkills = skillService.getStudentSkills(userId);
        summary.setTotalSkillsAdded(studentSkills.size());
        summary.setTotalProjects((int) projectRepository.countByUserId(userId));
        summary.setTotalCertifications((int) certificationRepository.countByUserId(userId));

        // 6. Strongest skills (top 3 by proficiency)
        List<Map<String, Object>> strongest = studentSkills.stream()
                .sorted((a, b) -> Integer.compare(b.getProficiencyLevel(), a.getProficiencyLevel()))
                .limit(3)
                .map(ss -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("skillName", ss.getSkillName());
                    map.put("category", ss.getSkillCategory());
                    map.put("proficiencyLevel", ss.getProficiencyLevel());
                    map.put("proficiencyLabel", ss.getProficiencyLabel());
                    return map;
                })
                .collect(Collectors.toList());
        summary.setStrongestSkills(strongest);

        // 7. Learning recommendations
        long completedLearning = recommendationRepository.countByUserIdAndStatus(userId, LearningStatus.COMPLETED);
        List<LearningRecommendationResponse> allRecs = learningPathService.getRecommendations(userId);
        summary.setCompletedLearningItems((int) completedLearning);
        summary.setTotalLearningItems(allRecs.size());

        List<LearningRecommendationResponse> pending = allRecs.stream()
                .filter(r -> !"COMPLETED".equalsIgnoreCase(r.getStatus()))
                .limit(3)
                .collect(Collectors.toList());
        summary.setPendingRecommendations(pending);

        return summary;
    }
}
