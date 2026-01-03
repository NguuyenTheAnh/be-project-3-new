package com.theanh.lms.dto;

import lombok.Data;

@Data
public class LessonPreviewDto {
    private Long id;
    private String title;
    private String lessonType;
    private Integer durationSeconds;
    private Boolean isPreview;
    private java.util.List<DocumentResponse> documents;
}
