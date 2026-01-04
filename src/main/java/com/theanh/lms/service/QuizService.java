package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.QuizDto;
import com.theanh.lms.entity.Quiz;

public interface QuizService extends BaseService<Quiz, QuizDto, Long> {

    QuizDto findActiveByLesson(Long lessonId);

    QuizDto findActiveById(Long id);
}
