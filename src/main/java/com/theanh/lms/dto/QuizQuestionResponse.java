package com.theanh.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizQuestionResponse {
    private Long id;
    private String questionText;
    private String questionType;
    private Integer position;
    private java.math.BigDecimal points;
    private String explanation;
    private List<QuizAnswerResponse> answers;
}
