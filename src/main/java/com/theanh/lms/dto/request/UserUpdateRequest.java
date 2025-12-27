package com.theanh.lms.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserUpdateRequest {
    @Size(max = 255)
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private Long avatarFileId;
    private Set<String> roles;
    private Boolean isActive;
    private Boolean isDeleted;
}
