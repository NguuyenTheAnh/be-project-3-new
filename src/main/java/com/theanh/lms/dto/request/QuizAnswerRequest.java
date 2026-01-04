package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizAnswerRequest {
    @NotNull
    private String answerText;
    private Boolean isCorrect;
    private Integer position;
}
