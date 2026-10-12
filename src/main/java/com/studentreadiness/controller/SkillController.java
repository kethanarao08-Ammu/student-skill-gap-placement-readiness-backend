package com.studentreadiness.controller;

import com.studentreadiness.dto.StudentSkillRequest;
import com.studentreadiness.dto.StudentSkillResponse;
import com.studentreadiness.entity.Skill;
import com.studentreadiness.enums.SkillCategory;
import com.studentreadiness.security.SecurityUtils;
import com.studentreadiness.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Skill>> getSkillsByCategory(@PathVariable SkillCategory category) {
        return ResponseEntity.ok(skillService.getSkillsByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }

    @GetMapping("/student")
    public ResponseEntity<List<StudentSkillResponse>> getStudentSkills() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(skillService.getStudentSkills(userId));
    }

    @PostMapping("/student")
    public ResponseEntity<StudentSkillResponse> addOrUpdateStudentSkill(@Valid @RequestBody StudentSkillRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        StudentSkillResponse response = skillService.addOrUpdateStudentSkill(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/student/{skillId}")
    public ResponseEntity<Map<String, String>> deleteStudentSkill(@PathVariable Long skillId) {
        Long userId = SecurityUtils.getCurrentUserId();
        skillService.deleteStudentSkill(userId, skillId);
        return ResponseEntity.ok(Map.of("message", "Skill successfully removed from profile"));
    }
}
