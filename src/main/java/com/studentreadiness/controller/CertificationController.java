package com.studentreadiness.controller;

import com.studentreadiness.dto.CertificationRequest;
import com.studentreadiness.dto.CertificationResponse;
import com.studentreadiness.security.SecurityUtils;
import com.studentreadiness.service.CertificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/certifications")
public class CertificationController {

    private final CertificationService certificationService;

    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @GetMapping
    public ResponseEntity<List<CertificationResponse>> getCertifications() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(certificationService.getCertifications(userId));
    }

    @PostMapping
    public ResponseEntity<CertificationResponse> createCertification(@Valid @RequestBody CertificationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        CertificationResponse response = certificationService.createCertification(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificationResponse> updateCertification(@PathVariable Long id,
                                                                     @Valid @RequestBody CertificationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        CertificationResponse response = certificationService.updateCertification(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCertification(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        certificationService.deleteCertification(userId, id);
        return ResponseEntity.ok(Map.of("message", "Certification deleted successfully"));
    }
}
