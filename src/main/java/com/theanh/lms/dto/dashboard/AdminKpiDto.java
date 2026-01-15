package com.theanh.lms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminKpiDto {
    private Long totalUsers;
    private Long totalInstructors;
    private Long totalCourses;
    private Long publishedCourses;
    private Long enrollmentsLast30Days;
    private Long revenueToday;
    private Long revenueLast7Days;
    private Long revenueLast30Days;
}
