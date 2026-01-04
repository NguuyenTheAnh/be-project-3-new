package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.AnswerDto;
import com.theanh.lms.entity.Answer;
import com.theanh.lms.repository.AnswerRepository;
import com.theanh.lms.service.AnswerService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerServiceImpl extends BaseServiceImpl<Answer, AnswerDto, Long> implements AnswerService {

    private final AnswerRepository repository;

    public AnswerServiceImpl(AnswerRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public List<AnswerDto> findByQuestion(Long questionId) {
        return repository.findByQuestion(questionId)
                .stream()
                .map(a -> modelMapper.map(a, AnswerDto.class))
                .toList();
    }

    @Override
    public AnswerDto findActiveById(Long id) {
        return repository.findActiveById(id)
                .map(a -> modelMapper.map(a, AnswerDto.class))
                .orElse(null);
    }

    @Override
    protected Class<Answer> getEntityClass() {
        return Answer.class;
    }

    @Override
    protected Class<AnswerDto> getDtoClass() {
        return AnswerDto.class;
    }
}
