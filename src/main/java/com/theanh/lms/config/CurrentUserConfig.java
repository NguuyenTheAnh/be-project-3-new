package com.theanh.lms.config;

import com.theanh.common.security.CurrentUserProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Configuration
public class CurrentUserConfig {

    @Bean
    public CurrentUserProvider currentUserProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && StringUtils.hasText(authentication.getName())) {
                return Optional.of(authentication.getName());
            }
            return Optional.of("system");
        };
    }
}
