package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentReportRequest {
    @NotBlank
    private String targetType; // REVIEW/QUESTION/ANSWER/COURSE/LESSON
    @NotNull
    private Long targetId;
    private String reason;
}
