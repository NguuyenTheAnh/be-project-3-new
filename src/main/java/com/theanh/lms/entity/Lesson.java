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
@Table(name = "lesson")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Lesson extends BaseAuditEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "lesson_type", nullable = false, length = 50)
    private String lessonType;

    @Column(name = "content_text", columnDefinition = "longtext")
    private String contentText;

    @Column(name = "video_file_id")
    private Long videoFileId;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "is_free_preview")
    private Boolean isFreePreview;
}
