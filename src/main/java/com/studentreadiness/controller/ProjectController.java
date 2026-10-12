package com.studentreadiness.controller;

import com.studentreadiness.dto.ProjectRequest;
import com.studentreadiness.dto.ProjectResponse;
import com.studentreadiness.security.SecurityUtils;
import com.studentreadiness.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(projectService.getProjects(userId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ProjectResponse response = projectService.createProject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id,
                                                         @Valid @RequestBody ProjectRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ProjectResponse response = projectService.updateProject(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        projectService.deleteProject(userId, id);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }
}
