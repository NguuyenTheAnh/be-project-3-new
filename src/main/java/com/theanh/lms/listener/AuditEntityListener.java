package com.theanh.lms.listener;

import com.theanh.common.base.BaseEntity;
import com.theanh.common.security.CurrentUserProvider;
import com.theanh.lms.config.SpringContextHolder;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class AuditEntityListener {

    @PrePersist
    public void setCreatedUser(Object target) {
        if (target instanceof BaseEntity entity) {
            String user = resolveUser();
            if (!StringUtils.hasText(entity.getCreatedUser())) {
                entity.setCreatedUser(user);
            }
            entity.setUpdatedUser(user);
        }
    }

    @PreUpdate
    public void setUpdatedUser(Object target) {
        if (target instanceof BaseEntity entity) {
            String user = resolveUser();
            entity.setUpdatedUser(user);
        }
    }

    private String resolveUser() {
        try {
            CurrentUserProvider provider = SpringContextHolder.getBean(CurrentUserProvider.class);
            if (provider != null) {
                return provider.getCurrentUsername().orElse("system");
            }
        } catch (Exception e) {
            log.debug("Cannot resolve current user, fallback to system", e);
        }
        return "system";
    }
}
