package com.theanh.lms.dto;

import lombok.Data;

@Data
public class QuizAnswerResponse {
    private Long id;
    private String answerText;
    private Boolean isCorrect;
    private Integer position;
}
