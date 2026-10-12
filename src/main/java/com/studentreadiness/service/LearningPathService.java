package com.studentreadiness.service;

import com.studentreadiness.dto.LearningRecommendationResponse;
import com.studentreadiness.dto.SkillGapItem;
import com.studentreadiness.dto.SkillGapResponse;
import com.studentreadiness.entity.*;
import com.studentreadiness.enums.LearningStatus;
import com.studentreadiness.enums.PriorityLevel;
import com.studentreadiness.exception.AccessDeniedException;
import com.studentreadiness.exception.ResourceNotFoundException;
import com.studentreadiness.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningPathService {

    private final LearningRecommendationRepository recommendationRepository;
    private final LearningResourceRepository resourceRepository;
    private final SkillGapService skillGapService;
    private final JobRoleRepository jobRoleRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;

    public LearningPathService(LearningRecommendationRepository recommendationRepository,
                               LearningResourceRepository resourceRepository,
                               SkillGapService skillGapService,
                               JobRoleRepository jobRoleRepository,
                               SkillRepository skillRepository,
                               UserRepository userRepository,
                               StudentProfileRepository profileRepository) {
        this.recommendationRepository = recommendationRepository;
        this.resourceRepository = resourceRepository;
        this.skillGapService = skillGapService;
        this.jobRoleRepository = jobRoleRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public List<LearningRecommendationResponse> generateRecommendations(Long userId, Long jobRoleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "userId", userId));

        SkillGapResponse gap = skillGapService.calculateSkillGap(userId, jobRoleId);
        JobRole role = jobRoleRepository.findById(gap.getJobRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("JobRole", "id", gap.getJobRoleId()));

        // Delete existing uncompleted recommendations for this specific role to refresh them
        // Preserve any completed items
        List<LearningRecommendation> existing = recommendationRepository.findByUserIdAndJobRoleId(userId, role.getId());
        Set<Long> completedSkillIds = existing.stream()
                .filter(r -> r.getStatus() == LearningStatus.COMPLETED)
                .map(r -> r.getSkill().getId())
                .collect(Collectors.toSet());

        // Delete only non-completed ones
        existing.stream()
                .filter(r -> r.getStatus() != LearningStatus.COMPLETED)
                .forEach(recommendationRepository::delete);

        List<SkillGapItem> gapSkills = new ArrayList<>();
        gapSkills.addAll(gap.getHighPriorityGaps());
        gap.getNeedsImprovementSkills().forEach(s -> {
            if (gapSkills.stream().noneMatch(g -> g.getSkillId().equals(s.getSkillId()))) {
                gapSkills.add(s);
            }
        });
        gap.getMissingSkills().forEach(s -> {
            if (gapSkills.stream().noneMatch(g -> g.getSkillId().equals(s.getSkillId()))) {
                gapSkills.add(s);
            }
        });

        for (SkillGapItem item : gapSkills) {
            // If already marked completed by user, skip regenerating
            if (completedSkillIds.contains(item.getSkillId())) {
                continue;
            }

            Skill skill = skillRepository.findById(item.getSkillId()).orElse(null);
            if (skill == null) continue;

            PriorityLevel priority;
            if (item.getGap() >= 3 || (item.getGap() >= 2 && item.getImportanceWeight() >= 4)) {
                priority = PriorityLevel.HIGH;
            } else if (item.getGap() >= 2 || item.getImportanceWeight() >= 3) {
                priority = PriorityLevel.MEDIUM;
            } else {
                priority = PriorityLevel.LOW;
            }

            String reason = String.format("Current proficiency is %d/5, but role requires %d/5 (Gap: %d, Importance: %d/5)",
                    item.getCurrentLevel(), item.getRequiredLevel(), item.getGap(), item.getImportanceWeight());

            List<LearningResource> resources = resourceRepository.findBySkillId(skill.getId());
            LearningResource resource = resources.isEmpty() ? null : resources.get(0);

            String suggestedTopic = resource != null ? resource.getTopic() : skill.getName() + " Fundamentals";
            String resourceTitle = resource != null ? resource.getTitle() : "Mastering " + skill.getName();
            String resourceUrl = resource != null ? resource.getUrl() : "https://devdocs.io/" + skill.getName().toLowerCase().replace(" ", "-");
            String duration = resource != null ? resource.getEstimatedDuration() : "8 Hours";

            LearningRecommendation rec = new LearningRecommendation(
                    user,
                    profile,
                    role,
                    skill,
                    priority,
                    reason,
                    suggestedTopic,
                    resourceTitle,
                    resourceUrl,
                    duration,
                    LearningStatus.NOT_STARTED
            );

            recommendationRepository.save(rec);
        }

        return getRecommendations(userId);
    }

    @Transactional(readOnly = true)
    public List<LearningRecommendationResponse> getRecommendations(Long userId) {
        return recommendationRepository.findByUserIdOrderByPriorityAsc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LearningRecommendationResponse updateStatus(Long userId, Long recommendationId, LearningStatus newStatus) {
        LearningRecommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("LearningRecommendation", "id", recommendationId));

        if (!rec.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to modify this learning item");
        }

        rec.setStatus(newStatus);
        LearningRecommendation saved = recommendationRepository.save(rec);
        return mapToResponse(saved);
    }

    private LearningRecommendationResponse mapToResponse(LearningRecommendation r) {
        LearningRecommendationResponse resp = new LearningRecommendationResponse();
        resp.setId(r.getId());
        resp.setSkillId(r.getSkill().getId());
        resp.setSkillName(r.getSkill().getName());
        if (r.getJobRole() != null) {
            resp.setJobRoleId(r.getJobRole().getId());
            resp.setJobRoleTitle(r.getJobRole().getTitle());
        }
        resp.setPriority(r.getPriority().name());
        resp.setReason(r.getReason());
        resp.setSuggestedTopic(r.getSuggestedTopic());
        resp.setResourceTitle(r.getResourceTitle());
        resp.setResourceUrl(r.getResourceUrl());
        resp.setEstimatedDuration(r.getEstimatedDuration());
        resp.setStatus(r.getStatus().name());
        resp.setCreatedAt(r.getCreatedAt());
        resp.setUpdatedAt(r.getUpdatedAt());
        return resp;
    }
}
