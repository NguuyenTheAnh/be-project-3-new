package com.theanh.lms.dto;

import com.theanh.common.base.BaseDto;
import com.theanh.lms.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDto extends BaseDto {
    private String email;
    private String fullName;
    private Long avatarFileId;
    private String phone;
    private LocalDate dateOfBirth;
    private LocalDateTime lastLoginAt;
    private UserStatus status;
    private Set<String> roles;
}
