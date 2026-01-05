package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.CourseDto;
import com.theanh.lms.entity.Course;
import com.theanh.lms.repository.CourseRepository;
import com.theanh.lms.service.CourseService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends BaseServiceImpl<Course, CourseDto, Long> implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.courseRepository = repository;
    }

    @Override
    public CourseDto findActiveById(Long id) {
        return courseRepository.findActiveById(id)
                .map(c -> modelMapper.map(c, CourseDto.class))
                .orElse(null);
    }

    @Override
    public CourseDto findActivePublishedById(Long id) {
        return courseRepository.findActivePublishedById(id)
                .map(c -> modelMapper.map(c, CourseDto.class))
                .orElse(null);
    }

    @Override
    protected Class<Course> getEntityClass() {
        return Course.class;
    }

    @Override
    protected Class<CourseDto> getDtoClass() {
        return CourseDto.class;
    }
}
