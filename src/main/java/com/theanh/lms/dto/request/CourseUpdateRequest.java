package com.theanh.lms.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CourseUpdateRequest {
    private String title;
    private String slug;
    private Long categoryId;
    private String shortDescription;
    private String description;
    private String level;
    private String language;
    private Long thumbnailFileId;
    private Long introVideoFileId;
    private List<Long> tagIds;
}
