package com.theanh.lms.entity;

import com.theanh.lms.common.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "moderation_action")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModerationAction extends BaseAuditEntity {

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "moderator_user_id", nullable = false)
    private Long moderatorUserId;

    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;
}
