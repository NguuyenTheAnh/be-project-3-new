package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.QuizAnswerDto;
import com.theanh.lms.entity.QuizAnswer;

import java.util.List;

public interface QuizAnswerService extends BaseService<QuizAnswer, QuizAnswerDto, Long> {

    List<QuizAnswerDto> findByQuestion(Long questionId);
}
