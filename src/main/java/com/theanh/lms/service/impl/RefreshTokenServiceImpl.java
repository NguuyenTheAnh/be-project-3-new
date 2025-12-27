package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.entity.RefreshToken;
import com.theanh.lms.entity.User;
import com.theanh.lms.repository.RefreshTokenRepository;
import com.theanh.lms.service.RefreshTokenService;
import com.theanh.lms.utils.ChecksumUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public RefreshToken create(User user, long ttlMs, String deviceId, String userAgent, String ipAddress) {
        String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(rawToken)
                .issuedAt(now)
                .expiresAt(now.plusNanos(ttlMs * 1_000_000))
                .deviceId(deviceId)
                .userAgent(cut(userAgent, 500))
                .ipAddress(cut(ipAddress, 64))
                .build();
        refreshTokenRepository.save(refreshToken);
        refreshToken.setTokenHash(rawToken); // return raw token to caller
        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken verifyAndRotate(String rawToken, long ttlMs, String deviceId, String userAgent, String ipAddress) {
        RefreshToken existing = resolve(rawToken);
        existing.setRevokedAt(LocalDateTime.now());
        existing.setRevokeReason("ROTATED");
        refreshTokenRepository.save(existing);

        RefreshToken newToken = create(
                userWithId(existing.getUserId()),
                ttlMs,
                deviceId,
                userAgent,
                ipAddress
        );
        existing.setReplacedById(newToken.getId());
        refreshTokenRepository.save(existing);
        return newToken;
    }

    @Override
    @Transactional
    public void revoke(String rawToken, String reason) {
        RefreshToken token = resolve(rawToken);
        token.setRevokedAt(LocalDateTime.now());
        token.setRevokeReason(StringUtils.hasText(reason) ? reason : "USER_LOGOUT");
        refreshTokenRepository.save(token);
    }

    private RefreshToken resolve(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            throw new BusinessException("auth.token.invalid");
        }
        RefreshToken token = refreshTokenRepository.findByTokenHash(rawToken)
                .orElseThrow(() -> new BusinessException("auth.token.invalid"));
        if (token.getRevokedAt() != null) {
            throw new BusinessException("auth.token.revoked");
        }
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("auth.token.expired");
        }
        return token;
    }

    private String cut(String value, int max) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private User userWithId(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
