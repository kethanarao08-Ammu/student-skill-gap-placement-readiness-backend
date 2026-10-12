package com.studentreadiness.controller;

import com.studentreadiness.entity.JobRole;
import com.studentreadiness.service.JobRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job-roles")
public class JobRoleController {

    private final JobRoleService jobRoleService;

    public JobRoleController(JobRoleService jobRoleService) {
        this.jobRoleService = jobRoleService;
    }

    @GetMapping
    public ResponseEntity<List<JobRole>> getAllActiveJobRoles() {
        return ResponseEntity.ok(jobRoleService.getAllActiveJobRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobRole> getJobRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(jobRoleService.getJobRoleById(id));
    }
}
