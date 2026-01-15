package com.theanh.lms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorKpiDto {
    private Long myPublishedCourses;
    private Long totalStudents;
    private Long newEnrollmentsLast7Days;
    private Long newEnrollmentsLast30Days;
    private Long revenueLast7Days;
    private Long revenueLast30Days;
    private BigDecimal avgRating;
}
