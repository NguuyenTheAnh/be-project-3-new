package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.CourseInstructorDto;
import com.theanh.lms.entity.CourseInstructor;
import com.theanh.lms.repository.CourseInstructorRepository;
import com.theanh.lms.service.CourseInstructorService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public void deleteByCourseId(Long courseId) {
        List<Long> ids = ((CourseInstructorRepository) repository).findByCourseIdAndIsDeletedFalse(courseId)
                .stream().map(CourseInstructor::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.deleteByIds(ids);
        }
    }

    @Override
    public boolean isInstructorOfCourse(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            return false;
        }
        return ((CourseInstructorRepository) repository).countActiveByCourseAndUser(courseId, userId) > 0;
    }
}
