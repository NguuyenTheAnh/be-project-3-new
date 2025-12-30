package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.CourseInstructorDto;
import com.theanh.lms.entity.CourseInstructor;
import com.theanh.lms.repository.CourseInstructorRepository;
import com.theanh.lms.service.CourseInstructorService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class CourseInstructorServiceImpl extends BaseServiceImpl<CourseInstructor, CourseInstructorDto, Long> implements CourseInstructorService {

    public CourseInstructorServiceImpl(CourseInstructorRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    protected Class<CourseInstructor> getEntityClass() {
        return CourseInstructor.class;
    }

    @Override
    protected Class<CourseInstructorDto> getDtoClass() {
        return CourseInstructorDto.class;
    }
}
