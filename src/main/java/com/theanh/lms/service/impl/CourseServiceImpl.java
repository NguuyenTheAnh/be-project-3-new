package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.CourseDto;
import com.theanh.lms.entity.Course;
import com.theanh.lms.repository.CourseRepository;
import com.theanh.lms.service.CourseService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends BaseServiceImpl<Course, CourseDto, Long> implements CourseService {

    public CourseServiceImpl(CourseRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
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
