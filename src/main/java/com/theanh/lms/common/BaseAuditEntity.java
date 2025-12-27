package com.theanh.lms.common;

import com.theanh.common.base.BaseEntity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

@MappedSuperclass
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
