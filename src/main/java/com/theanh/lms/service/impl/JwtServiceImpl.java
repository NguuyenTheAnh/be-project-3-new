package com.theanh.lms.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.theanh.lms.config.JwtProperties;
import com.theanh.lms.entity.User;
import com.theanh.lms.service.JwtService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        this.verifier = JWT.require(algorithm)
                .withIssuer(jwtProperties.getIssuer())
                .build();
    }

    @Override
    public String generateAccessToken(User user, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.getAccessTokenExpirationMs());
        return JWT.create()
                .withIssuer(jwtProperties.getIssuer())
                .withSubject(user.getId().toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .withClaim("email", user.getEmail())
                .withClaim("roles", roles)
                .sign(algorithm);
    }

    @Override
    public String extractSubject(String token) {
        return verifier.verify(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
