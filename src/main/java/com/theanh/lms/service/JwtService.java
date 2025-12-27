package com.theanh.lms.service;

import com.theanh.lms.entity.User;
import java.util.List;

public interface JwtService {

    String generateAccessToken(User user, List<String> roles);

    String extractSubject(String token);

    boolean isTokenValid(String token);
}
