package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.QuizAttemptAnswerDto;
import com.theanh.lms.dto.QuizAttemptDto;
import com.theanh.lms.dto.request.QuizAttemptSubmitRequest;
import com.theanh.lms.service.QuizAttemptService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping("/quizzes/{quizId}/attempts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<QuizAttemptDto>> startAttempt(@PathVariable @NotNull Long quizId) {
        Long userId = currentUserId();
        return ResponseConfig.success(quizAttemptService.startAttempt(userId, quizId));
    }

    @PostMapping("/quizzes/{quizId}/attempts/{attemptId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<QuizAttemptDto>> submitAttempt(@PathVariable @NotNull Long quizId,
                                                                     @PathVariable @NotNull Long attemptId,
                                                                     @RequestBody @Valid QuizAttemptSubmitRequest request) {
        Long userId = currentUserId();
        List<QuizAttemptAnswerDto> answers = request.getAnswers().stream().map(a -> {
            QuizAttemptAnswerDto dto = new QuizAttemptAnswerDto();
            dto.setQuestionId(a.getQuestionId());
            dto.setAnswerId(a.getAnswerId());
            dto.setAnswerText(a.getAnswerText());
            return dto;
        }).collect(Collectors.toList());
        return ResponseConfig.success(quizAttemptService.submitAttempt(userId, quizId, attemptId, answers));
    }

    @GetMapping("/quizzes/{quizId}/attempts/{attemptId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<QuizAttemptDto>> getAttempt(@PathVariable @NotNull Long quizId,
                                                                  @PathVariable @NotNull Long attemptId) {
        Long userId = currentUserId();
        return ResponseConfig.success(quizAttemptService.getAttempt(userId, quizId, attemptId));
    }

    @GetMapping("/quizzes/{quizId}/attempts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<List<QuizAttemptDto>>> listAttempts(@PathVariable @NotNull Long quizId) {
        Long userId = currentUserId();
        return ResponseConfig.success(quizAttemptService.listAttempts(userId, quizId));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getPrincipal().toString());
    }
}
