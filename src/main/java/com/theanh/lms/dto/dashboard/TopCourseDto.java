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
public class TopCourseDto {
    private Long courseId;
    private String courseTitle;
    private Long enrollmentCount;
    private Long revenue;
    private BigDecimal ratingAvg;
    private Long ratingCount;
}
