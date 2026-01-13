package com.theanh.lms.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseProgressResponse {
    private Long courseId;
    private Long totalLessons;
    private Long completedLessons;
    private BigDecimal progressPercent;
}
