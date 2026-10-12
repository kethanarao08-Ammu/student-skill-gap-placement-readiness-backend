package com.studentreadiness.service;

import com.studentreadiness.dto.SkillGapResponse;
import com.studentreadiness.entity.*;
import com.studentreadiness.enums.SkillCategory;
import com.studentreadiness.enums.SkillStatus;
import com.studentreadiness.repository.JobRoleRepository;
import com.studentreadiness.repository.StudentProfileRepository;
import com.studentreadiness.repository.StudentSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillGapServiceTest {

    @Mock
    private JobRoleRepository jobRoleRepository;

    @Mock
    private StudentSkillRepository studentSkillRepository;

    @Mock
    private StudentProfileRepository profileRepository;

    @InjectMocks
    private SkillGapService skillGapService;

    private JobRole javaBackendRole;
    private Skill javaSkill;
    private Skill springBootSkill;
    private Skill mySqlSkill;

    @BeforeEach
    void setUp() {
        javaBackendRole = new JobRole(1L, "Java Backend Developer", "Backend development with Java", true);

        javaSkill = new Skill(1L, "Java", SkillCategory.PROGRAMMING_LANGUAGES, "Core Java");
        springBootSkill = new Skill(2L, "Spring Boot", SkillCategory.BACKEND, "Spring Boot Framework");
        mySqlSkill = new Skill(3L, "MySQL", SkillCategory.DATABASES, "Relational Database");

        JobRoleSkill jrs1 = new JobRoleSkill(javaBackendRole, javaSkill, 4, 5); // Req 4, Weight 5
        JobRoleSkill jrs2 = new JobRoleSkill(javaBackendRole, springBootSkill, 3, 4); // Req 3, Weight 4
        JobRoleSkill jrs3 = new JobRoleSkill(javaBackendRole, mySqlSkill, 3, 3); // Req 3, Weight 3

        List<JobRoleSkill> required = new ArrayList<>(List.of(jrs1, jrs2, jrs3));
        javaBackendRole.setRequiredSkills(required);
    }

    @Test
    void testCalculateSkillGap_PartialMatch() {
        Long userId = 2L;
        Long roleId = 1L;

        when(jobRoleRepository.findById(roleId)).thenReturn(Optional.of(javaBackendRole));

        // Student has Java at level 3 (gap 1), Spring Boot at level 3 (gap 0), MySQL not added (gap 3)
        User student = new User(userId, "student@skillgap.edu", "pass", "Alex", null, true);
        StudentSkill ss1 = new StudentSkill(student, javaSkill, 3);
        StudentSkill ss2 = new StudentSkill(student, springBootSkill, 3);

        when(studentSkillRepository.findByUserId(userId)).thenReturn(List.of(ss1, ss2));

        SkillGapResponse response = skillGapService.calculateSkillGap(userId, roleId);

        assertNotNull(response);
        assertEquals(1L, response.getJobRoleId());
        assertEquals("Java Backend Developer", response.getJobRoleTitle());
        assertEquals(3, response.getTotalRequiredSkills());
        assertEquals(1, response.getMatchedSkillsCount()); // Spring Boot
        assertEquals(1, response.getNeedsImprovementCount()); // Java
        assertEquals(1, response.getMissingSkillsCount()); // MySQL

        // Expected earned simple: Java min(3,4)=3, SpringBoot min(3,3)=3, MySQL 0. Total req = 4+3+3=10.
        // Simple % = (6 / 10) * 100 = 60.0%
        assertEquals(60.0, response.getSimpleMatchPercentage(), 0.1);

        // Expected weighted earned:
        // Java: 3 * 5 = 15
        // SpringBoot: 3 * 4 = 12
        // MySQL: 0 * 3 = 0
        // Total earned weighted = 27
        // Total required weighted = (4*5) + (3*4) + (3*3) = 20 + 12 + 9 = 41
        // Weighted % = (27 / 41) * 100 = 65.9% (rounded to 65.9)
        assertEquals(65.9, response.getWeightedMatchPercentage(), 0.1);
    }

    @Test
    void testCalculateSkillGap_ZeroSkills() {
        Long userId = 2L;
        Long roleId = 1L;

        when(jobRoleRepository.findById(roleId)).thenReturn(Optional.of(javaBackendRole));
        when(studentSkillRepository.findByUserId(userId)).thenReturn(List.of());

        SkillGapResponse response = skillGapService.calculateSkillGap(userId, roleId);

        assertNotNull(response);
        assertEquals(0.0, response.getSimpleMatchPercentage());
        assertEquals(0.0, response.getWeightedMatchPercentage());
        assertEquals(3, response.getMissingSkillsCount());
        assertEquals(0, response.getMatchedSkillsCount());
    }
}
