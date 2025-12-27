package com.theanh.lms.service;

import com.theanh.lms.dto.UserDto;
import com.theanh.lms.dto.request.LoginRequest;
import com.theanh.lms.dto.request.LogoutRequest;
import com.theanh.lms.dto.request.RefreshTokenRequest;
import com.theanh.lms.dto.request.RegisterRequest;
import com.theanh.lms.dto.response.AuthTokenResponse;

public interface AuthService {

    AuthTokenResponse register(RegisterRequest request);

    AuthTokenResponse login(LoginRequest request);

    AuthTokenResponse refresh(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    UserDto currentUser();
}
