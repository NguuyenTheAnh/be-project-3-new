package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String lessonType; // VIDEO/ARTICLE/QUIZ
    private String contentText;
    private Long videoFileId;
    private Integer durationSeconds;
    private Boolean isFreePreview;
    private Long courseSectionId;
    private Integer position;
    private Boolean isPreview;
}
