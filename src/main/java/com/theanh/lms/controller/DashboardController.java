package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.dashboard.AdminDashboardDto;
import com.theanh.lms.dto.dashboard.InstructorDashboardDto;
import com.theanh.lms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<AdminDashboardDto>> getAdminDashboard(
            @RequestParam(value = "days", required = false, defaultValue = "30") Integer days,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        AdminDashboardDto dashboard = dashboardService.getAdminDashboard(days, limit);
        return ResponseConfig.success(dashboard);
    }

    @GetMapping("/instructor")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<InstructorDashboardDto>> getInstructorDashboard(
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long instructorId = Long.parseLong(auth.getPrincipal().toString());
        InstructorDashboardDto dashboard = dashboardService.getInstructorDashboard(instructorId, limit);
        return ResponseConfig.success(dashboard);
    }

    @GetMapping("/instructor/{instructorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<InstructorDashboardDto>> getInstructorDashboardById(
            @PathVariable Long instructorId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        InstructorDashboardDto dashboard = dashboardService.getInstructorDashboard(instructorId, limit);
        return ResponseConfig.success(dashboard);
    }
}
