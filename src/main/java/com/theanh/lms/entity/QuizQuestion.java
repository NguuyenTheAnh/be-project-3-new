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
@Table(name = "quiz_question")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuizQuestion extends BaseAuditEntity {

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "question_text", columnDefinition = "text")
    private String questionText;

    @Column(name = "question_type", length = 50)
    private String questionType;

    @Column(name = "position")
    private Integer position;

    @Column(name = "points")
    private java.math.BigDecimal points;

    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;
}
