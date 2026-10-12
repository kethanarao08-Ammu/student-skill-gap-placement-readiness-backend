package com.studentreadiness.service;

import com.studentreadiness.dto.ReadinessScoreResponse;
import com.studentreadiness.dto.SkillGapResponse;
import com.studentreadiness.entity.AssessmentAttempt;
import com.studentreadiness.repository.AssessmentAttemptRepository;
import com.studentreadiness.repository.CertificationRepository;
import com.studentreadiness.repository.StudentProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadinessServiceTest {

    @Mock
    private SkillGapService skillGapService;

    @Mock
    private AssessmentAttemptRepository assessmentAttemptRepository;

    @Mock
    private StudentProjectRepository projectRepository;

    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private ReadinessService readinessService;

    @Test
    void testCalculateReadiness_CompleteProfile() {
        Long userId = 2L;
        Long roleId = 1L;

        SkillGapResponse mockGap = new SkillGapResponse();
        mockGap.setJobRoleId(roleId);
        mockGap.setJobRoleTitle("Java Backend Developer");
        mockGap.setWeightedMatchPercentage(80.0);
        mockGap.setTotalRequiredSkills(5);

        when(skillGapService.calculateSkillGap(userId, roleId)).thenReturn(mockGap);

        AssessmentAttempt mockAttempt = new AssessmentAttempt();
        mockAttempt.setScorePercentage(BigDecimal.valueOf(85.0));
        when(assessmentAttemptRepository.findTopByUserIdOrderByAttemptedAtDesc(userId))
                .thenReturn(Optional.of(mockAttempt));

        when(projectRepository.countByUserId(userId)).thenReturn(3L); // 100% project score
        when(certificationRepository.countByUserId(userId)).thenReturn(2L); // 100% cert score

        ReadinessScoreResponse response = readinessService.calculateReadiness(userId, roleId);

        assertNotNull(response);
        // Formula:
        // Skills (40%): 80.0 * 0.40 = 32.0
        // Assessment (35%): 85.0 * 0.35 = 29.75
        // Projects (15%): 100.0 * 0.15 = 15.0
        // Certs (10%): 100.0 * 0.10 = 10.0
        // Total = 32.0 + 29.75 + 15.0 + 10.0 = 86.75 -> rounded to 86.8
        assertEquals(86.8, response.getOverallReadinessScore(), 0.1);
        assertEquals("Placement Ready", response.getReadinessCategory());
        assertFalse(response.isProvisional());
        assertTrue(response.getMissingComponents().isEmpty());
    }

    @Test
    void testCalculateReadiness_ProvisionalWhenMissingAssessmentAndProjects() {
        Long userId = 2L;
        Long roleId = 1L;

        SkillGapResponse mockGap = new SkillGapResponse();
        mockGap.setJobRoleId(roleId);
        mockGap.setJobRoleTitle("Java Backend Developer");
        mockGap.setWeightedMatchPercentage(50.0);
        mockGap.setTotalRequiredSkills(5);

        when(skillGapService.calculateSkillGap(userId, roleId)).thenReturn(mockGap);
        when(assessmentAttemptRepository.findTopByUserIdOrderByAttemptedAtDesc(userId)).thenReturn(Optional.empty());
        when(projectRepository.countByUserId(userId)).thenReturn(0L);
        when(certificationRepository.countByUserId(userId)).thenReturn(0L);

        ReadinessScoreResponse response = readinessService.calculateReadiness(userId, roleId);

        assertNotNull(response);
        assertTrue(response.isProvisional());
        assertNotNull(response.getProvisionalReason());
        assertTrue(response.getMissingComponents().contains("Technical Assessment"));
        assertTrue(response.getMissingComponents().contains("Practical Projects"));
        assertTrue(response.getMissingComponents().contains("Industry Certifications"));

        // Overall: 50.0 * 0.40 = 20.0
        assertEquals(20.0, response.getOverallReadinessScore(), 0.1);
        assertEquals("Needs Strong Improvement", response.getReadinessCategory());
    }
}
