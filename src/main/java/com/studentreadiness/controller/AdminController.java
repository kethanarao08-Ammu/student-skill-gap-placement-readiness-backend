package com.studentreadiness.controller;

import com.studentreadiness.dto.JobRoleSkillRequest;
import com.studentreadiness.entity.JobRole;
import com.studentreadiness.entity.Skill;
import com.studentreadiness.service.AdminService;
import com.studentreadiness.service.JobRoleService;
import com.studentreadiness.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final SkillService skillService;
    private final JobRoleService jobRoleService;

    public AdminController(AdminService adminService,
                           SkillService skillService,
                           JobRoleService jobRoleService) {
        this.adminService = adminService;
        this.skillService = skillService;
        this.jobRoleService = jobRoleService;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(adminService.getAdminOverview());
    }

    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> getStudents() {
        return ResponseEntity.ok(adminService.getAllStudentsWithReadiness());
    }

    // Skill management
    @PostMapping("/skills")
    public ResponseEntity<Skill> createSkill(@Valid @RequestBody Skill skill) {
        Skill created = skillService.createSkill(skill);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @Valid @RequestBody Skill skill) {
        Skill updated = skillService.updateSkill(id, skill);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Map<String, String>> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok(Map.of("message", "Skill successfully deleted"));
    }

    // Job role management
    @PostMapping("/job-roles")
    public ResponseEntity<JobRole> createJobRole(@Valid @RequestBody JobRole jobRole) {
        JobRole created = jobRoleService.createJobRole(jobRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/job-roles/{id}")
    public ResponseEntity<JobRole> updateJobRole(@PathVariable Long id, @Valid @RequestBody JobRole jobRole) {
        JobRole updated = jobRoleService.updateJobRole(id, jobRole);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/job-roles/{id}")
    public ResponseEntity<Map<String, String>> deleteJobRole(@PathVariable Long id) {
        jobRoleService.deleteJobRole(id);
        return ResponseEntity.ok(Map.of("message", "Job role successfully deleted"));
    }

    @PostMapping("/job-roles/{id}/skills")
    public ResponseEntity<JobRole> addSkillToJobRole(
            @PathVariable Long id,
            @Valid @RequestBody JobRoleSkillRequest request) {
        JobRole updated = jobRoleService.addSkillToRole(
                id,
                request.getSkillId(),
                request.getRequiredLevel(),
                request.getImportanceWeight()
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/job-roles/{id}/skills/{skillId}")
    public ResponseEntity<JobRole> removeSkillFromJobRole(
            @PathVariable Long id,
            @PathVariable Long skillId) {
        JobRole updated = jobRoleService.removeSkillFromRole(id, skillId);
        return ResponseEntity.ok(updated);
    }
}
