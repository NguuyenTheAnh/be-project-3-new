package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.QuestionDto;
import com.theanh.lms.entity.Question;
import com.theanh.lms.enums.QuestionStatus;
import com.theanh.lms.repository.QuestionRepository;
import com.theanh.lms.service.QuestionService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends BaseServiceImpl<Question, QuestionDto, Long> implements QuestionService {

    private final QuestionRepository repository;

    public QuestionServiceImpl(QuestionRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public QuestionDto findActiveById(Long id) {
        return repository.findActiveById(id)
                .map(q -> modelMapper.map(q, QuestionDto.class))
                .orElse(null);
    }

    @Override
    public Page<QuestionDto> listByCourse(Long courseId, Pageable pageable) {
        return repository.findByCourse(courseId, pageable)
                .map(q -> modelMapper.map(q, QuestionDto.class));
    }

    @Override
    public Page<QuestionDto> listByCourse(Long courseId, Long lessonId, Pageable pageable) {
        if (lessonId == null) {
            return repository.findByCourseAndNoLesson(courseId, pageable)
                    .map(q -> modelMapper.map(q, QuestionDto.class));
        }
        return repository.findByCourseAndLesson(courseId, lessonId, pageable)
                .map(q -> modelMapper.map(q, QuestionDto.class));
    }

    @Override
    public QuestionDto saveObject(QuestionDto dto) {
        if (dto.getStatus() == null) {
            dto.setStatus(QuestionStatus.OPEN.name());
        }
        return super.saveObject(dto);
    }

    @Override
    protected Class<Question> getEntityClass() {
        return Question.class;
    }

    @Override
    protected Class<QuestionDto> getDtoClass() {
        return QuestionDto.class;
    }
}
