package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.UserResponse;
import com.theanh.lms.dto.request.LoginRequest;
import com.theanh.lms.dto.request.LogoutRequest;
import com.theanh.lms.dto.request.RefreshTokenRequest;
import com.theanh.lms.dto.request.RegisterRequest;
import com.theanh.lms.dto.UserDto;
import com.theanh.lms.dto.response.AuthTokenResponse;
import com.theanh.lms.service.AuthService;
import com.theanh.lms.utils.UserViewMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserViewMapper userViewMapper;

    @PostMapping("/register")
    public ResponseEntity<ResponseDto<AuthTokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseConfig.success(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<AuthTokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseConfig.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<AuthTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseConfig.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<String>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseConfig.success("logged_out");
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<UserResponse>> me() {
        return ResponseConfig.success(userViewMapper.toResponse(authService.currentUser()));
    }
}
