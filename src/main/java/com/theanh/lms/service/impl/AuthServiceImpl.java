package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.config.JwtProperties;
import com.theanh.lms.dto.UserDto;
import com.theanh.lms.dto.request.LoginRequest;
import com.theanh.lms.dto.request.LogoutRequest;
import com.theanh.lms.dto.request.RefreshTokenRequest;
import com.theanh.lms.dto.request.RegisterRequest;
import com.theanh.lms.dto.response.AuthTokenResponse;
import com.theanh.lms.entity.RefreshToken;
import com.theanh.lms.entity.User;
import com.theanh.lms.enums.RoleName;
import com.theanh.lms.service.AuthService;
import com.theanh.lms.service.JwtService;
import com.theanh.lms.service.RefreshTokenService;
import com.theanh.lms.service.RoleService;
import com.theanh.lms.service.UserService;
import com.theanh.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        userService.findActiveByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new BusinessException("data.exists");
                });
        roleService.ensureRole(RoleName.STUDENT.name(), "Student");
        var createRequest = new com.theanh.lms.dto.request.UserCreateRequest();
        createRequest.setEmail(request.getEmail());
        createRequest.setPassword(request.getPassword());
        createRequest.setFullName(request.getFullName());
        createRequest.setRoles(Set.of(RoleName.STUDENT.name()));
        userService.createUser(createRequest);
        User saved = userService.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("data.not_found"));
        return issueTokens(saved, List.of(RoleName.STUDENT.asAuthority()));
    }

    @Override
    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        User user = userService.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("auth.invalid_credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("auth.invalid_credentials");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        Set<String> roles = userService.findRoles(user.getId());
        List<String> authorities = roles.stream().map(r -> "ROLE_" + r).toList();
        return issueTokens(user, authorities);
    }

    @Override
    @Transactional
    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken rotated = refreshTokenService.verifyAndRotate(
                request.getRefreshToken(),
                jwtProperties.getRefreshTokenExpirationMs(),
                null,
                null,
                null
        );
        Long userId = rotated.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        Set<String> roles = userService.findRoles(userId);
        List<String> authorities = roles.stream().map(r -> "ROLE_" + r).toList();
        AuthTokenResponse response = issueTokens(user, authorities);
        response.setRefreshToken(rotated.getTokenHash());
        return response;
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.getRefreshToken(), "USER_LOGOUT");
    }

    @Override
    public UserDto currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("auth.unauthorized");
        }
        String userIdStr = Optional.ofNullable(authentication.getPrincipal())
                .map(Object::toString)
                .orElseThrow(() -> new BusinessException("auth.unauthorized"));
        Long userId = Long.parseLong(userIdStr);
        return userService.findById(userId);
    }

    private AuthTokenResponse issueTokens(User user, List<String> authorities) {
        String accessToken = jwtService.generateAccessToken(user, authorities);
        RefreshToken refreshToken = refreshTokenService.create(
                user,
                jwtProperties.getRefreshTokenExpirationMs(),
                null,
                null,
                null
        );
        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getTokenHash())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .build();
    }
}
