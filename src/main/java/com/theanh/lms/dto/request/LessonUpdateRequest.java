package com.theanh.lms.dto.request;

import lombok.Data;

@Data
public class LessonUpdateRequest {
    private String title;
    private String lessonType;
    private String contentText;
    private Long videoFileId;
    private Integer durationSeconds;
    private Boolean isFreePreview;
    private Long courseSectionId;
    private Integer position;
    private Boolean isPreview;
}
