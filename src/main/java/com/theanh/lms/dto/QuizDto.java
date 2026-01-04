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
public class QuizDto extends BaseDto {
    private Long lessonId;
    private String title;
    private Integer timeLimitSeconds;
    private BigDecimal passScore;
    private Integer maxAttempts;
    private Boolean shuffleQuestions;
}
