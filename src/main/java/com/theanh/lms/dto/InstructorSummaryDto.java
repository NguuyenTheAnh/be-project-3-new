package com.theanh.lms.dto;

import lombok.Data;

@Data
public class InstructorSummaryDto {
    private Long id;
    private String fullName;
    private UploadedFileDto avatarFile;
}
