package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonDocumentRequest {
    @NotNull
    private Long uploadedFileId;
    private String title;
    private Integer position;
}
