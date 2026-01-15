package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.UserDto;
import com.theanh.lms.dto.request.ProfileUpdateRequest;
import com.theanh.lms.dto.request.UserCreateRequest;
import com.theanh.lms.dto.request.UserUpdateRequest;
import com.theanh.lms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface UserService extends BaseService<User, UserDto, Long> {

    UserDto createUser(UserCreateRequest request);

    UserDto updateUser(Long id, UserUpdateRequest request);

    UserDto updateProfile(Long id, ProfileUpdateRequest request);

    Optional<User> findActiveByEmail(String email);

    Set<String> findRoles(Long userId);

    Page<UserDto> search(String keyword, Pageable pageable);

    Page<UserDto> searchByRole(String roleName, String keyword, Pageable pageable);
}
