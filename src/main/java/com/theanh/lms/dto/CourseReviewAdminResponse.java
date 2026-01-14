package com.theanh.lms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseReviewAdminResponse extends CourseReviewDto {
    private String courseTitle;
    private String studentName;
}
