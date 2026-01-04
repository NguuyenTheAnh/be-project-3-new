package com.theanh.lms.dto;

import com.theanh.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuizAttemptAnswerDto extends BaseDto {
    private Long quizAttemptId;
    private Long questionId;
    private Long answerId;
    private String answerText;
    private Boolean isCorrect;
    private BigDecimal pointsAwarded;
}
