package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.LessonDto;
import com.theanh.lms.entity.Lesson;
import com.theanh.lms.repository.LessonRepository;
import com.theanh.lms.service.LessonService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class LessonServiceImpl extends BaseServiceImpl<Lesson, LessonDto, Long> implements LessonService {

    public LessonServiceImpl(LessonRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    protected Class<Lesson> getEntityClass() {
        return Lesson.class;
    }

    @Override
    protected Class<LessonDto> getDtoClass() {
        return LessonDto.class;
    }
}
