package com.theanh.lms.service.impl;

import com.theanh.lms.dto.dashboard.*;
import com.theanh.lms.repository.DashboardRepository;
import com.theanh.lms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboard(Integer days, Integer limit) {
        if (days == null)
            days = 30;
        if (limit == null)
            limit = 10;

        // Build KPI
        AdminKpiDto kpi = buildAdminKpi();

        // Build trends
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<RevenueTrendDto> revenueTrend = buildRevenueTrend(startDate, limit);
        List<EnrollmentTrendDto> enrollmentTrend = buildEnrollmentTrend(startDate, limit);
        List<CourseFunnelDto> courseFunnel = buildCourseFunnel();

        // Build operational panels
        List<RefundQueueDto> refundQueue = buildRefundQueue(limit);
        List<ContentReportQueueDto> contentReportQueue = buildContentReportQueue(limit);

        // Build top tables
        List<TopCourseDto> topCoursesByRevenue = buildTopCoursesByRevenue(limit);
        List<TopCourseDto> topCoursesByEnrollment = buildTopCoursesByEnrollment(limit);
        List<TopCourseDto> lowRatingCourses = buildLowRatingCourses(5, limit);

        return AdminDashboardDto.builder()
                .kpi(kpi)
                .revenueTrend(revenueTrend)
                .enrollmentTrend(enrollmentTrend)
                .courseFunnel(courseFunnel)
                .refundQueue(refundQueue)
                .contentReportQueue(contentReportQueue)
                .topCoursesByRevenue(topCoursesByRevenue)
                .topCoursesByEnrollment(topCoursesByEnrollment)
                .lowRatingCourses(lowRatingCourses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorDashboardDto getInstructorDashboard(Long instructorId, Integer limit) {
        if (limit == null)
            limit = 10;

        // Build KPI
        InstructorKpiDto kpi = buildInstructorKpi(instructorId);

        // Build engagement
        List<UnansweredQuestionDto> unansweredQuestions = buildUnansweredQuestions(instructorId, limit);

        // Build my courses
        List<TopCourseDto> myCourses = buildInstructorCourses(instructorId, limit);

        // Build course completions
        List<CourseCompletionDto> courseCompletions = buildCourseCompletions(instructorId, limit);

        return InstructorDashboardDto.builder()
                .kpi(kpi)
                .unansweredQuestions(unansweredQuestions)
                .myCourses(myCourses)
                .courseCompletions(courseCompletions)
                .build();
    }

    // ==================== ADMIN BUILDERS ====================

    private AdminKpiDto buildAdminKpi() {
        Long totalUsers = dashboardRepository.countTotalUsers();
        Long totalInstructors = dashboardRepository.countTotalInstructors();
        Long totalCourses = dashboardRepository.countTotalCourses();
        Long publishedCourses = dashboardRepository.countPublishedCourses();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime last7Days = now.minusDays(7);
        LocalDateTime last30Days = now.minusDays(30);

        Long enrollmentsLast30Days = dashboardRepository.countEnrollmentsSince(last30Days);
        Long revenueToday = dashboardRepository.sumRevenueBetween(today, now);
        Long revenueLast7Days = dashboardRepository.sumRevenueBetween(last7Days, now);
        Long revenueLast30Days = dashboardRepository.sumRevenueBetween(last30Days, now);

        return AdminKpiDto.builder()
                .totalUsers(totalUsers != null ? totalUsers : 0L)
                .totalInstructors(totalInstructors != null ? totalInstructors : 0L)
                .totalCourses(totalCourses != null ? totalCourses : 0L)
                .publishedCourses(publishedCourses != null ? publishedCourses : 0L)
                .enrollmentsLast30Days(enrollmentsLast30Days != null ? enrollmentsLast30Days : 0L)
                .revenueToday(revenueToday != null ? revenueToday : 0L)
                .revenueLast7Days(revenueLast7Days != null ? revenueLast7Days : 0L)
                .revenueLast30Days(revenueLast30Days != null ? revenueLast30Days : 0L)
                .build();
    }

    private List<RevenueTrendDto> buildRevenueTrend(LocalDateTime startDate, int limit) {
        List<Object[]> results = dashboardRepository.getRevenueTrend(startDate, limit);
        List<RevenueTrendDto> trends = new ArrayList<>();
        for (Object[] row : results) {
            String date = row[0] != null ? row[0].toString() : "";
            Long revenue = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Long orderCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            trends.add(RevenueTrendDto.builder()
                    .date(date)
                    .revenue(revenue)
                    .orderCount(orderCount)
                    .build());
        }
        return trends;
    }

    private List<EnrollmentTrendDto> buildEnrollmentTrend(LocalDateTime startDate, int limit) {
        List<Object[]> results = dashboardRepository.getEnrollmentTrend(startDate, limit);
        List<EnrollmentTrendDto> trends = new ArrayList<>();
        for (Object[] row : results) {
            String date = row[0] != null ? row[0].toString() : "";
            Long enrollmentCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            trends.add(EnrollmentTrendDto.builder()
                    .date(date)
                    .enrollmentCount(enrollmentCount)
                    .build());
        }
        return trends;
    }

    private List<CourseFunnelDto> buildCourseFunnel() {
        List<Object[]> results = dashboardRepository.getCourseFunnel();
        List<CourseFunnelDto> funnel = new ArrayList<>();
        for (Object[] row : results) {
            String status = row[0] != null ? row[0].toString() : "";
            Long courseCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            funnel.add(CourseFunnelDto.builder()
                    .status(status)
                    .courseCount(courseCount)
                    .build());
        }
        return funnel;
    }

    private List<RefundQueueDto> buildRefundQueue(int limit) {
        List<Object[]> results = dashboardRepository.getRefundQueue(limit);
        List<RefundQueueDto> queue = new ArrayList<>();
        for (Object[] row : results) {
            queue.add(RefundQueueDto.builder()
                    .refundRequestId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .orderId(row[1] != null ? ((Number) row[1]).longValue() : null)
                    .userId(row[2] != null ? ((Number) row[2]).longValue() : null)
                    .userFullName(row[3] != null ? row[3].toString() : null)
                    .reason(row[4] != null ? row[4].toString() : null)
                    .requestedAmountCents(row[5] != null ? ((Number) row[5]).longValue() : null)
                    .status(row[6] != null ? row[6].toString() : null)
                    .requestedAt(row[7] != null ? ((java.sql.Timestamp) row[7]).toLocalDateTime() : null)
                    .build());
        }
        return queue;
    }

    private List<ContentReportQueueDto> buildContentReportQueue(int limit) {
        List<Object[]> results = dashboardRepository.getContentReportQueue(limit);
        List<ContentReportQueueDto> queue = new ArrayList<>();
        for (Object[] row : results) {
            queue.add(ContentReportQueueDto.builder()
                    .reportId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .contentType(row[1] != null ? row[1].toString() : null)
                    .contentId(row[2] != null ? ((Number) row[2]).longValue() : null)
                    .reporterUserId(row[3] != null ? ((Number) row[3]).longValue() : null)
                    .reporterFullName(row[4] != null ? row[4].toString() : null)
                    .reason(row[5] != null ? row[5].toString() : null)
                    .status(row[6] != null ? row[6].toString() : null)
                    .reportedAt(row[7] != null ? ((java.sql.Timestamp) row[7]).toLocalDateTime() : null)
                    .build());
        }
        return queue;
    }

    private List<TopCourseDto> buildTopCoursesByRevenue(int limit) {
        return buildTopCourses(dashboardRepository.getTopCoursesByRevenue(limit));
    }

    private List<TopCourseDto> buildTopCoursesByEnrollment(int limit) {
        return buildTopCourses(dashboardRepository.getTopCoursesByEnrollment(limit));
    }

    private List<TopCourseDto> buildLowRatingCourses(int minRatingCount, int limit) {
        return buildTopCourses(dashboardRepository.getLowRatingCourses(minRatingCount, limit));
    }

    private List<TopCourseDto> buildTopCourses(List<Object[]> results) {
        List<TopCourseDto> courses = new ArrayList<>();
        for (Object[] row : results) {
            courses.add(TopCourseDto.builder()
                    .courseId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .courseTitle(row[1] != null ? row[1].toString() : null)
                    .enrollmentCount(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                    .revenue(row[3] != null ? ((Number) row[3]).longValue() : 0L)
                    .ratingAvg(row[4] != null ? BigDecimal.valueOf(((Number) row[4]).doubleValue()) : null)
                    .ratingCount(row[5] != null ? ((Number) row[5]).longValue() : 0L)
                    .build());
        }
        return courses;
    }

    // ==================== INSTRUCTOR BUILDERS ====================

    private InstructorKpiDto buildInstructorKpi(Long instructorId) {
        Long myPublishedCourses = dashboardRepository.countInstructorPublishedCourses(instructorId);
        Long totalStudents = dashboardRepository.countInstructorTotalStudents(instructorId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last7Days = now.minusDays(7);
        LocalDateTime last30Days = now.minusDays(30);

        Long newEnrollmentsLast7Days = dashboardRepository.countInstructorEnrollmentsSince(instructorId, last7Days);
        Long newEnrollmentsLast30Days = dashboardRepository.countInstructorEnrollmentsSince(instructorId, last30Days);
        Long revenueLast7Days = dashboardRepository.sumInstructorRevenueSince(instructorId, last7Days);
        Long revenueLast30Days = dashboardRepository.sumInstructorRevenueSince(instructorId, last30Days);
        Double avgRating = dashboardRepository.getInstructorAvgRating(instructorId);

        return InstructorKpiDto.builder()
                .myPublishedCourses(myPublishedCourses != null ? myPublishedCourses : 0L)
                .totalStudents(totalStudents != null ? totalStudents : 0L)
                .newEnrollmentsLast7Days(newEnrollmentsLast7Days != null ? newEnrollmentsLast7Days : 0L)
                .newEnrollmentsLast30Days(newEnrollmentsLast30Days != null ? newEnrollmentsLast30Days : 0L)
                .revenueLast7Days(revenueLast7Days != null ? revenueLast7Days : 0L)
                .revenueLast30Days(revenueLast30Days != null ? revenueLast30Days : 0L)
                .avgRating(avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO)
                .build();
    }

    private List<UnansweredQuestionDto> buildUnansweredQuestions(Long instructorId, int limit) {
        List<Object[]> results = dashboardRepository.getInstructorUnansweredQuestions(instructorId, limit);
        List<UnansweredQuestionDto> questions = new ArrayList<>();
        for (Object[] row : results) {
            questions.add(UnansweredQuestionDto.builder()
                    .questionId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .courseId(row[1] != null ? ((Number) row[1]).longValue() : null)
                    .courseTitle(row[2] != null ? row[2].toString() : null)
                    .lessonId(row[3] != null ? ((Number) row[3]).longValue() : null)
                    .lessonTitle(row[4] != null ? row[4].toString() : null)
                    .questionTitle(row[5] != null ? row[5].toString() : null)
                    .questionContent(row[6] != null ? row[6].toString() : null)
                    .askerUserId(row[7] != null ? ((Number) row[7]).longValue() : null)
                    .askerFullName(row[8] != null ? row[8].toString() : null)
                    .askedAt(row[9] != null ? ((java.sql.Timestamp) row[9]).toLocalDateTime() : null)
                    .build());
        }
        return questions;
    }

    private List<TopCourseDto> buildInstructorCourses(Long instructorId, int limit) {
        return buildTopCourses(dashboardRepository.getInstructorCourses(instructorId, limit));
    }

    private List<CourseCompletionDto> buildCourseCompletions(Long instructorId, int limit) {
        List<Object[]> results = dashboardRepository.getInstructorCourseCompletions(instructorId, limit);
        List<CourseCompletionDto> completions = new ArrayList<>();
        for (Object[] row : results) {
            completions.add(CourseCompletionDto.builder()
                    .courseId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .courseTitle(row[1] != null ? row[1].toString() : null)
                    .totalLessons(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                    .totalEnrollments(row[3] != null ? ((Number) row[3]).longValue() : 0L)
                    .totalCompletedLessons(row[4] != null ? ((Number) row[4]).longValue() : 0L)
                    .completionRate(row[5] != null
                            ? BigDecimal.valueOf(((Number) row[5]).doubleValue()).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO)
                    .build());
        }
        return completions;
    }
}
