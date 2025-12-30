package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PresignPutRequest {

    @NotBlank
    private String filename;

    @NotBlank
    private String contentType;

    private Long size;

    @NotNull
    private String purpose; // THUMBNAIL | INTRO_VIDEO | LESSON_VIDEO | DOCUMENT

    private Long courseId;
    private Long lessonId;
    private Boolean isPublic = false;
}
