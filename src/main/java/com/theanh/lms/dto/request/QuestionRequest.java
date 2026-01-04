package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotNull
    private Long courseId;
    private Long lessonId;
}
