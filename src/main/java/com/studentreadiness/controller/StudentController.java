package com.studentreadiness.controller;

import com.studentreadiness.dto.StudentProfileRequest;
import com.studentreadiness.dto.StudentProfileResponse;
import com.studentreadiness.security.SecurityUtils;
import com.studentreadiness.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponse> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        StudentProfileResponse profile = studentService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<StudentProfileResponse> updateProfile(@Valid @RequestBody StudentProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        StudentProfileResponse updated = studentService.updateProfile(userId, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/profile/completion")
    public ResponseEntity<Map<String, Integer>> getProfileCompletion() {
        Long userId = SecurityUtils.getCurrentUserId();
        int completion = studentService.calculateProfileCompletion(userId);
        return ResponseEntity.ok(Map.of("completionPercentage", completion));
    }
}
