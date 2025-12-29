package com.theanh.lms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserResponse extends UserDto {
    private UploadedFileDto avatarFile;
}
