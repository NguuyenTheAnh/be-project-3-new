package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.QuizDto;
import com.theanh.lms.entity.Quiz;
import com.theanh.lms.repository.QuizRepository;
import com.theanh.lms.service.QuizService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class QuizServiceImpl extends BaseServiceImpl<Quiz, QuizDto, Long> implements QuizService {

    private final QuizRepository quizRepository;

    public QuizServiceImpl(QuizRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.quizRepository = repository;
    }

    @Override
    public QuizDto findActiveByLesson(Long lessonId) {
        return quizRepository.findActiveByLesson(lessonId)
                .map(q -> modelMapper.map(q, QuizDto.class))
                .orElse(null);
    }

    @Override
    public QuizDto findActiveById(Long id) {
        return quizRepository.findActiveById(id)
                .map(q -> modelMapper.map(q, QuizDto.class))
                .orElse(null);
    }

    @Override
    protected Class<Quiz> getEntityClass() {
        return Quiz.class;
    }

    @Override
    protected Class<QuizDto> getDtoClass() {
        return QuizDto.class;
    }
}
