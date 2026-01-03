package com.theanh.lms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseDetailResponse {
    private Long id;
    private String title;
    private String slug;
    private String shortDescription;
    private String description;
    private String level;
    private String language;
    private String status;
    private LocalDateTime publishedAt;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private CategoryDto category;
    private List<TagDto> tags;
    private UploadedFileDto thumbnail;
    private UploadedFileDto introVideo;
    private List<InstructorSummaryDto> instructors;
    private List<CourseSectionResponse> sections;
    private List<DocumentResponse> courseDocuments;
}
