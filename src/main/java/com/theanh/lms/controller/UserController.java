package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.UserDto;
import com.theanh.lms.dto.request.ProfileUpdateRequest;
import com.theanh.lms.dto.request.UserCreateRequest;
import com.theanh.lms.dto.request.UserUpdateRequest;
import com.theanh.lms.service.UserService;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<Page<UserDto>>> list(@RequestParam(value = "q", required = false) String keyword, Pageable pageable) {
        Page<UserDto> page = userService.search(keyword, pageable);
        return ResponseConfig.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<UserDto>> get(@PathVariable Long id) {
        return ResponseConfig.success(userService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<UserDto>> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseConfig.success(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<UserDto>> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseConfig.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseConfig.success("deleted");
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ResponseDto<UserDto>> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        return ResponseConfig.success(userService.updateProfile(userId, request));
    }
}
