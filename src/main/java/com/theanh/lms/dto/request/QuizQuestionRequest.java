package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuizQuestionRequest {
    @NotNull
    private String questionText;
    @NotNull
    private String questionType; // SINGLE/MULTI/TRUE_FALSE/TEXT
    private Integer position;
    private BigDecimal points;
    private String explanation;
}
