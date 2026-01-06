package com.theanh.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class LessonDetailResponse {
    private Long id;
    private Long courseId;
    private Long courseSectionId;
    private String title;
    private String lessonType;
    private String contentText;
    private Integer durationSeconds;
    private Boolean isFreePreview;
    private Boolean isPreview;
    private UploadedFileDto videoFile;
    private List<DocumentResponse> documents;
}
