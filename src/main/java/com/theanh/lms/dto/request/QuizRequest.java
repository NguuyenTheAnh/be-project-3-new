package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuizRequest {
    @NotNull
    private Long lessonId;
    private String title;
    private Integer timeLimitSeconds;
    private BigDecimal passScore;
    private Integer maxAttempts;
    private Boolean shuffleQuestions;
}
