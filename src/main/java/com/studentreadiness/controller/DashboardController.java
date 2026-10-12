package com.studentreadiness.controller;

import com.studentreadiness.dto.DashboardSummaryResponse;
import com.studentreadiness.security.SecurityUtils;
import com.studentreadiness.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        Long userId = SecurityUtils.getCurrentUserId();
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(summary);
    }
}
