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

import java.math.BigDecimal;

@Entity
@Table(name = "quiz_attempt_answer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuizAttemptAnswer extends BaseAuditEntity {

    @Column(name = "quiz_attempt_id", nullable = false)
    private Long quizAttemptId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "answer_text", columnDefinition = "text")
    private String answerText;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "points_awarded")
    private BigDecimal pointsAwarded;
}
