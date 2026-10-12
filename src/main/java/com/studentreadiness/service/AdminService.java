package com.studentreadiness.service;

import com.studentreadiness.dto.ReadinessScoreResponse;
import com.studentreadiness.entity.JobRole;
import com.studentreadiness.entity.StudentProfile;
import com.studentreadiness.entity.User;
import com.studentreadiness.enums.Role;
import com.studentreadiness.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final JobRoleRepository jobRoleRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final ReadinessService readinessService;
    private final StudentService studentService;

    public AdminService(UserRepository userRepository,
                        StudentProfileRepository profileRepository,
                        SkillRepository skillRepository,
                        JobRoleRepository jobRoleRepository,
                        AssessmentAttemptRepository attemptRepository,
                        ReadinessService readinessService,
                        StudentService studentService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.skillRepository = skillRepository;
        this.jobRoleRepository = jobRoleRepository;
        this.attemptRepository = attemptRepository;
        this.readinessService = readinessService;
        this.studentService = studentService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminOverview() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalStudents = userRepository.countByRole(Role.ROLE_STUDENT);
        long totalSkills = skillRepository.count();
        long totalJobRoles = jobRoleRepository.count();
        long totalAttempts = attemptRepository.count();

        stats.put("totalStudents", totalStudents);
        stats.put("totalSkills", totalSkills);
        stats.put("totalJobRoles", totalJobRoles);
        stats.put("totalAssessmentsTaken", totalAttempts);

        // Average readiness calculation across all students
        List<User> students = userRepository.findByRole(Role.ROLE_STUDENT);
        double totalReadiness = 0.0;
        int evaluatedStudents = 0;

        Map<String, Integer> targetRoleDistribution = new HashMap<>();

        for (User student : students) {
            Optional<StudentProfile> profOpt = profileRepository.findByUserId(student.getId());
            if (profOpt.isPresent() && profOpt.get().getTargetJobRole() != null) {
                JobRole target = profOpt.get().getTargetJobRole();
                targetRoleDistribution.put(target.getTitle(),
                        targetRoleDistribution.getOrDefault(target.getTitle(), 0) + 1);
            }
            try {
                ReadinessScoreResponse r = readinessService.calculateReadiness(student.getId(), null);
                totalReadiness += r.getOverallReadinessScore();
                evaluatedStudents++;
            } catch (Exception ignored) {
            }
        }

        double avgReadiness = evaluatedStudents > 0 ? Math.round((totalReadiness / evaluatedStudents) * 10.0) / 10.0 : 0.0;
        stats.put("averageReadinessScore", avgReadiness);
        stats.put("targetRoleDistribution", targetRoleDistribution);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllStudentsWithReadiness() {
        List<User> students = userRepository.findByRole(Role.ROLE_STUDENT);
        List<Map<String, Object>> result = new ArrayList<>();

        for (User s : students) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", s.getId());
            map.put("email", s.getEmail());
            map.put("fullName", s.getFullName());
            map.put("createdAt", s.getCreatedAt());

            Optional<StudentProfile> profOpt = profileRepository.findByUserId(s.getId());
            if (profOpt.isPresent()) {
                StudentProfile prof = profOpt.get();
                map.put("college", prof.getCollege());
                map.put("degree", prof.getDegree());
                map.put("branch", prof.getBranch());
                map.put("cgpa", prof.getCgpa());
                map.put("graduationYear", prof.getGraduationYear());
                map.put("targetRole", prof.getTargetJobRole() != null ? prof.getTargetJobRole().getTitle() : "Not set");
            } else {
                map.put("college", "N/A");
                map.put("degree", "N/A");
                map.put("branch", "N/A");
                map.put("cgpa", null);
                map.put("graduationYear", null);
                map.put("targetRole", "Not set");
            }

            map.put("profileCompletion", studentService.calculateProfileCompletion(s.getId()));

            try {
                ReadinessScoreResponse r = readinessService.calculateReadiness(s.getId(), null);
                map.put("readinessScore", r.getOverallReadinessScore());
                map.put("readinessCategory", r.getReadinessCategory());
                map.put("isProvisional", r.isProvisional());
            } catch (Exception e) {
                map.put("readinessScore", 0.0);
                map.put("readinessCategory", "Needs Strong Improvement");
                map.put("isProvisional", true);
            }

            result.add(map);
        }

        return result;
    }
}
