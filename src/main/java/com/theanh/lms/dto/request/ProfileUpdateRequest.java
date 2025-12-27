package com.theanh.lms.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {
    @Size(max = 255)
    private String fullName;
    @Size(max = 50)
    private String phone;
    private LocalDate dateOfBirth;
    private Long avatarFileId;
}
