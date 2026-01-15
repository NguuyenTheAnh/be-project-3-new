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
public class CourseCompletionDto {
    private Long courseId;
    private String courseTitle;
    private Long totalLessons;
    private Long totalEnrollments;
    private Long totalCompletedLessons;
    private BigDecimal completionRate;
}
