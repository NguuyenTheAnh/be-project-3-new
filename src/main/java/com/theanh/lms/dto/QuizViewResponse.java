package com.theanh.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizViewResponse {
    private Long id;
    private Long lessonId;
    private String title;
    private Integer timeLimitSeconds;
    private java.math.BigDecimal passScore;
    private Integer maxAttempts;
    private Boolean shuffleQuestions;
    private List<QuizQuestionResponse> questions;
}
