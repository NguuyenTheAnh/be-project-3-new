package com.theanh.lms.service;

import com.theanh.lms.entity.RefreshToken;
import com.theanh.lms.entity.User;

public interface RefreshTokenService {

    RefreshToken create(User user, long ttlMs, String deviceId, String userAgent, String ipAddress);

    RefreshToken verifyAndRotate(String rawToken, long ttlMs, String deviceId, String userAgent, String ipAddress);

    void revoke(String rawToken, String reason);
}
