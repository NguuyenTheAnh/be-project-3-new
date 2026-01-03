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
@Table(name = "lesson_document")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LessonDocument extends BaseAuditEntity {

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "uploaded_file_id", nullable = false)
    private Long uploadedFileId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "position")
    private Integer position;
}
