package com.theanh.lms.common;

import com.theanh.common.base.BaseEntity;
import com.theanh.lms.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, AuditEntityListener.class})
public abstract class BaseAuditEntity extends BaseEntity {

    @PrePersist
    protected void applyDefaultFlags() {
        if (getIsActive() == null) {
            setIsActive(Boolean.TRUE);
        }
        if (getIsDeleted() == null) {
            setIsDeleted(Boolean.FALSE);
        }
    }
}
