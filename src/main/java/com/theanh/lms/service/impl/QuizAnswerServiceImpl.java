package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.QuizAnswerDto;
import com.theanh.lms.entity.QuizAnswer;
import com.theanh.lms.repository.QuizAnswerRepository;
import com.theanh.lms.service.QuizAnswerService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizAnswerServiceImpl extends BaseServiceImpl<QuizAnswer, QuizAnswerDto, Long> implements QuizAnswerService {

    private final QuizAnswerRepository repository;

    public QuizAnswerServiceImpl(QuizAnswerRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public List<QuizAnswerDto> findByQuestion(Long questionId) {
        return repository.findActiveByQuestion(questionId)
                .stream()
                .map(a -> modelMapper.map(a, QuizAnswerDto.class))
                .toList();
    }

    @Override
    protected Class<QuizAnswer> getEntityClass() {
        return QuizAnswer.class;
    }

    @Override
    protected Class<QuizAnswerDto> getDtoClass() {
        return QuizAnswerDto.class;
    }
}
