package com.theanh.lms.service;

import com.theanh.lms.dto.dashboard.AdminDashboardDto;
import com.theanh.lms.dto.dashboard.InstructorDashboardDto;

public interface DashboardService {

    AdminDashboardDto getAdminDashboard(Integer days, Integer limit);

    InstructorDashboardDto getInstructorDashboard(Long instructorId, Integer limit);
}
