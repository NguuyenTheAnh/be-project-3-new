package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.QuestionDto;
import com.theanh.lms.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionService extends BaseService<Question, QuestionDto, Long> {

    QuestionDto findActiveById(Long id);

    Page<QuestionDto> listByCourse(Long courseId, Pageable pageable);
}
