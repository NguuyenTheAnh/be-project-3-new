package com.theanh.lms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private AdminKpiDto kpi;
    private List<RevenueTrendDto> revenueTrend;
    private List<EnrollmentTrendDto> enrollmentTrend;
    private List<CourseFunnelDto> courseFunnel;
    private List<RefundQueueDto> refundQueue;
    private List<ContentReportQueueDto> contentReportQueue;
    private List<TopCourseDto> topCoursesByRevenue;
    private List<TopCourseDto> topCoursesByEnrollment;
    private List<TopCourseDto> lowRatingCourses;
}
