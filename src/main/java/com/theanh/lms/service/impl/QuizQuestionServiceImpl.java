package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.QuizQuestionDto;
import com.theanh.lms.entity.QuizQuestion;
import com.theanh.lms.repository.QuizQuestionRepository;
import com.theanh.lms.service.QuizQuestionService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizQuestionServiceImpl extends BaseServiceImpl<QuizQuestion, QuizQuestionDto, Long> implements QuizQuestionService {

    private final QuizQuestionRepository repository;

    public QuizQuestionServiceImpl(QuizQuestionRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public List<QuizQuestionDto> findByQuiz(Long quizId) {
        return repository.findActiveByQuiz(quizId)
                .stream()
                .map(q -> modelMapper.map(q, QuizQuestionDto.class))
                .toList();
    }

    @Override
    protected Class<QuizQuestion> getEntityClass() {
        return QuizQuestion.class;
    }

    @Override
    protected Class<QuizQuestionDto> getDtoClass() {
        return QuizQuestionDto.class;
    }
}
