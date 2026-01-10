package com.theanh.lms.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InstructorCourseListResponse {
    private Long id;
    private String title;
    private String slug;
    private String shortDescription;
    private String level;
    private String language;
    private String status;
    private Long priceCents;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private UploadedFileDto thumbnail;
}
