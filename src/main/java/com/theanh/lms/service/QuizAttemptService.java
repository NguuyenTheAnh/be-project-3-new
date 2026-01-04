package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.QuizAttemptDto;
import com.theanh.lms.dto.QuizAttemptAnswerDto;
import com.theanh.lms.entity.QuizAttempt;

import java.util.List;

public interface QuizAttemptService extends BaseService<QuizAttempt, QuizAttemptDto, Long> {

    QuizAttemptDto startAttempt(Long userId, Long quizId);

    QuizAttemptDto submitAttempt(Long userId, Long quizId, Long attemptId, List<QuizAttemptAnswerDto> answers);

    QuizAttemptDto getAttempt(Long userId, Long quizId, Long attemptId);

    List<QuizAttemptDto> listAttempts(Long userId, Long quizId);
}
