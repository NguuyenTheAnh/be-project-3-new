package com.theanh.lms.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseInstructorRequest {
    private Long userId;
    private String instructorRole;
    private BigDecimal revenueShare;
}
