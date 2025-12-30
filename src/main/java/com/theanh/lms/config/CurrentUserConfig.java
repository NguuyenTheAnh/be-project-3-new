package com.theanh.lms.config;

import com.theanh.common.security.CurrentUserProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Configuration
public class CurrentUserConfig {

    /**
     * Ưu tiên lấy email từ authentication details (được JwtAuthenticationFilter gắn vào),
     * fallback principal, sau đó chuẩn hóa username về phần trước dấu '@'.
     */
    @Bean
    @Primary
    public CurrentUserProvider currentUserProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String email = extractEmail(authentication);
                if (StringUtils.hasText(email)) {
                    return Optional.of(normalizeUserCode(email));
                }
                Object principal = authentication.getPrincipal();
                if (principal instanceof String principalStr && StringUtils.hasText(principalStr)) {
                    return Optional.of(normalizeUserCode(principalStr));
                }
            }
            return Optional.of("system");
        };
    }

    private String normalizeUserCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return "system";
        }
        int atIndex = raw.indexOf('@');
        if (atIndex > 0) {
            return raw.substring(0, atIndex);
        }
        return raw;
    }

    private String extractEmail(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof String s && s.contains("@")) {
            return s;
        }
        if (details instanceof java.util.Map<?, ?> map) {
            Object email = map.get("email");
            if (email instanceof String s && s.contains("@")) {
                return s;
            }
        }
        return null;
    }
}
