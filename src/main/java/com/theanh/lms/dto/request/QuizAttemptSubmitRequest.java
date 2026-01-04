package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuizAttemptSubmitRequest {
    @NotNull
    private Long quizId;
    @NotNull
    private List<QuizAttemptSubmitRequest.AnswerPayload> answers;

    @Data
    public static class AnswerPayload {
        @NotNull
        private Long questionId;
        private Long answerId;
        private String answerText;
    }
}
