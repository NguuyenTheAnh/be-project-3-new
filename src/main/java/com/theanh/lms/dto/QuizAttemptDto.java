package com.theanh.lms.dto;

import com.theanh.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuizAttemptDto extends BaseDto {
    private Long quizId;
    private Long userId;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private BigDecimal score;
    private String status;
}
