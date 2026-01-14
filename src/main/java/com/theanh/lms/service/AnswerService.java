package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.AnswerDto;
import com.theanh.lms.entity.Answer;

import java.util.List;

public interface AnswerService extends BaseService<Answer, AnswerDto, Long> {

    List<AnswerDto> findByQuestion(Long questionId);

    AnswerDto findActiveById(Long id);

    List<AnswerDto> findByQuestions(List<Long> questionIds);

    List<com.theanh.lms.dto.AnswerAdminResponse> findByQuestionsForManagement(List<Long> questionIds);

    List<com.theanh.lms.dto.AnswerAdminResponse> findByQuestionWithCreatedUser(Long questionId);

    List<com.theanh.lms.dto.AnswerAdminResponse> findApprovedByQuestion(Long questionId);
}
