package com.studentreadiness.service;

import com.studentreadiness.dto.SkillGapItem;
import com.studentreadiness.dto.SkillGapResponse;
import com.studentreadiness.entity.*;
import com.studentreadiness.enums.SkillStatus;
import com.studentreadiness.exception.ResourceNotFoundException;
import com.studentreadiness.repository.JobRoleRepository;
import com.studentreadiness.repository.StudentProfileRepository;
import com.studentreadiness.repository.StudentSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class SkillGapService {

    private final JobRoleRepository jobRoleRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final StudentProfileRepository profileRepository;

    public SkillGapService(JobRoleRepository jobRoleRepository,
                           StudentSkillRepository studentSkillRepository,
                           StudentProfileRepository profileRepository) {
        this.jobRoleRepository = jobRoleRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public SkillGapResponse calculateSkillGap(Long userId, Long jobRoleId) {
        JobRole role;
        if (jobRoleId != null) {
            role = jobRoleRepository.findById(jobRoleId)
                    .orElseThrow(() -> new ResourceNotFoundException("JobRole", "id", jobRoleId));
        } else {
            StudentProfile profile = profileRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "userId", userId));
            if (profile.getTargetJobRole() == null) {
                // If student has not set a target role, use the first available active role as default
                role = jobRoleRepository.findByActiveTrue().stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No active job roles found. Please select a target job role."));
            } else {
                role = profile.getTargetJobRole();
            }
        }

        // Get student's acquired skills mapped by skill ID
        List<StudentSkill> studentSkills = studentSkillRepository.findByUserId(userId);
        Map<Long, Integer> studentSkillMap = new HashMap<>();
        for (StudentSkill ss : studentSkills) {
            studentSkillMap.put(ss.getSkill().getId(), ss.getProficiencyLevel());
        }

        List<JobRoleSkill> requiredSkills = role.getRequiredSkills();

        List<SkillGapItem> matched = new ArrayList<>();
        List<SkillGapItem> needsImprovement = new ArrayList<>();
        List<SkillGapItem> missing = new ArrayList<>();
        List<SkillGapItem> highPriorityGaps = new ArrayList<>();

        double totalRequiredPoints = 0;
        double totalEarnedPoints = 0;

        double totalWeightedRequiredPoints = 0;
        double totalWeightedEarnedPoints = 0;

        for (JobRoleSkill req : requiredSkills) {
            Skill skill = req.getSkill();
            int requiredLevel = req.getRequiredLevel() > 0
                    ? req.getRequiredLevel()
                    : req.getRequiredProficiency();
            int weight = req.getImportanceWeight();
            int currentLevel = studentSkillMap.getOrDefault(skill.getId(), 0);
            int gap = Math.max(0, requiredLevel - currentLevel);

            SkillGapItem item = new SkillGapItem();
            item.setSkillId(skill.getId());
            item.setSkillName(skill.getName());
            item.setSkillCategory(skill.getCategory() != null ? skill.getCategory().name() : "");
            item.setCurrentLevel(currentLevel);
            item.setRequiredLevel(requiredLevel);
            item.setGap(gap);
            item.setImportanceWeight(weight);

            // Accumulate simple points
            int earnedSimple = Math.min(currentLevel, requiredLevel);
            totalEarnedPoints += earnedSimple;
            totalRequiredPoints += requiredLevel;

            // Accumulate weighted points
            totalWeightedEarnedPoints += (earnedSimple * weight);
            totalWeightedRequiredPoints += (requiredLevel * weight);

            if (currentLevel >= requiredLevel) {
                item.setStatus(SkillStatus.MATCHED);
                matched.add(item);
            } else if (currentLevel > 0) {
                item.setStatus(SkillStatus.NEEDS_IMPROVEMENT);
                needsImprovement.add(item);
            } else {
                item.setStatus(SkillStatus.MISSING);
                missing.add(item);
            }

            // High priority gap condition: has gap and (high weight >= 4 or significant gap >= 2)
            if (gap > 0 && (weight >= 4 || gap >= 2)) {
                item.setPriorityReason(String.format("Critical requirement (Weight: %d/5, Gap: %d levels)", weight, gap));
                highPriorityGaps.add(item);
            }
        }

        // Sort high priority gaps by weight descending, then gap descending
        highPriorityGaps.sort((a, b) -> {
            int cmp = Integer.compare(b.getImportanceWeight(), a.getImportanceWeight());
            return cmp != 0 ? cmp : Integer.compare(b.getGap(), a.getGap());
        });

        double simplePercentage = totalRequiredPoints > 0
                ? round((totalEarnedPoints / totalRequiredPoints) * 100.0)
                : 0.0;

        double weightedPercentage = totalWeightedRequiredPoints > 0
                ? round((totalWeightedEarnedPoints / totalWeightedRequiredPoints) * 100.0)
                : 0.0;

        SkillGapResponse response = new SkillGapResponse();
        response.setJobRoleId(role.getId());
        response.setJobRoleTitle(role.getTitle());
        response.setSimpleMatchPercentage(simplePercentage);
        response.setWeightedMatchPercentage(weightedPercentage);
        response.setScoringMethodActive("WEIGHTED");
        response.setMatchedSkillsCount(matched.size());
        response.setNeedsImprovementCount(needsImprovement.size());
        response.setMissingSkillsCount(missing.size());
        response.setTotalRequiredSkills(requiredSkills.size());
        response.setMatchedSkills(matched);
        response.setNeedsImprovementSkills(needsImprovement);
        response.setMissingSkills(missing);
        response.setHighPriorityGaps(highPriorityGaps);

        return response;
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
