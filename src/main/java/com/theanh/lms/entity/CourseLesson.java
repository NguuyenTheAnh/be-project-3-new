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
@Table(name = "course_lesson")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CourseLesson extends BaseAuditEntity {

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_section_id")
    private Long courseSectionId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "position")
    private Integer position;

    @Column(name = "is_preview")
    private Boolean isPreview;
}
