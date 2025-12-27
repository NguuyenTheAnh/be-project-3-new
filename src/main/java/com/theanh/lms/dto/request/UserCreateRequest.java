package com.theanh.lms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserCreateRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    @Size(max = 255)
    private String fullName;

    private String phone;
    private LocalDate dateOfBirth;
    private Long avatarFileId;
    private Set<String> roles;
}
