package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class CourseCreateRequest {
    @NotBlank
    private String title;
    private String slug;
    private Long categoryId;
    private String shortDescription;
    private String description;
    private String level;
    private String language;
    private Long thumbnailFileId;
    private Long introVideoFileId;
    @Min(0)
    private Long priceCents;
    private List<Long> tagIds;
    private List<CourseInstructorRequest> instructors;
}
