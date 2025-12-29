package com.theanh.lms.utils;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.dto.UserDto;
import com.theanh.lms.dto.UserResponse;
import com.theanh.lms.service.UploadedFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserViewMapper {

    private final UploadedFileService uploadedFileService;

    public UserResponse toResponse(UserDto dto) {
        if (dto == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setId(dto.getId());
        response.setEmail(dto.getEmail());
        response.setFullName(dto.getFullName());
        response.setAvatarFileId(dto.getAvatarFileId());
        response.setPhone(dto.getPhone());
        response.setDateOfBirth(dto.getDateOfBirth());
        response.setLastLoginAt(dto.getLastLoginAt());
        response.setStatus(dto.getStatus());
        response.setRoles(dto.getRoles());
        if (dto.getAvatarFileId() != null) {
            try {
                UploadedFileDto avatar = uploadedFileService.findById(dto.getAvatarFileId());
                response.setAvatarFile(avatar);
            } catch (BusinessException ex) {
                // ignore missing avatar, keep null
            }
        }
        return response;
    }

    public List<UserResponse> toResponseList(List<UserDto> list) {
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
