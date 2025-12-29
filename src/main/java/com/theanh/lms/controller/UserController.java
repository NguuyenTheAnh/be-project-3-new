package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.UserDto;
import com.theanh.lms.dto.UserResponse;
import com.theanh.lms.dto.request.ProfileUpdateRequest;
import com.theanh.lms.dto.request.UserCreateRequest;
import com.theanh.lms.dto.request.UserUpdateRequest;
import com.theanh.lms.service.UserService;
import com.theanh.lms.utils.UserViewMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserViewMapper userViewMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<Page<UserResponse>>> list(@RequestParam(value = "q", required = false) String keyword, Pageable pageable) {
        Page<UserDto> page = userService.search(keyword, pageable);
        Page<UserResponse> mapped = page.map(userViewMapper::toResponse);
        return ResponseConfig.success(mapped);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<UserResponse>> get(@PathVariable Long id) {
        return ResponseConfig.success(userViewMapper.toResponse(userService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<UserResponse>> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseConfig.success(userViewMapper.toResponse(userService.createUser(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<UserResponse>> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseConfig.success(userViewMapper.toResponse(userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseConfig.success("deleted");
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ResponseDto<UserResponse>> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        return ResponseConfig.success(userViewMapper.toResponse(userService.updateProfile(userId, request)));
    }
}
