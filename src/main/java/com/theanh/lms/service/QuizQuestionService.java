package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.QuizQuestionDto;
import com.theanh.lms.entity.QuizQuestion;

import java.util.List;

public interface QuizQuestionService extends BaseService<QuizQuestion, QuizQuestionDto, Long> {

    List<QuizQuestionDto> findByQuiz(Long quizId);
}
