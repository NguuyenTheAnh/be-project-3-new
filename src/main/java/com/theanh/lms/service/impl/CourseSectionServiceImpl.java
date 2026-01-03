package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.CourseSectionDto;
import com.theanh.lms.entity.CourseSection;
import com.theanh.lms.repository.CourseSectionRepository;
import com.theanh.lms.service.CourseSectionService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseSectionServiceImpl extends BaseServiceImpl<CourseSection, CourseSectionDto, Long> implements CourseSectionService {

    public CourseSectionServiceImpl(CourseSectionRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    protected Class<CourseSection> getEntityClass() {
        return CourseSection.class;
    }

    @Override
    protected Class<CourseSectionDto> getDtoClass() {
        return CourseSectionDto.class;
    }

    @Override
    public List<CourseSectionDto> findByCourseId(Long courseId) {
        return ((CourseSectionRepository) repository).findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .filter(sec -> !Boolean.TRUE.equals(sec.getIsDeleted()))
                .map(sec -> modelMapper.map(sec, CourseSectionDto.class))
                .toList();
    }
}
