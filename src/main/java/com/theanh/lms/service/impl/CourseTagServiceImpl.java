package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.CourseTagDto;
import com.theanh.lms.entity.CourseTag;
import com.theanh.lms.repository.CourseTagRepository;
import com.theanh.lms.service.CourseTagService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseTagServiceImpl extends BaseServiceImpl<CourseTag, CourseTagDto, Long> implements CourseTagService {

    public CourseTagServiceImpl(CourseTagRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    protected Class<CourseTag> getEntityClass() {
        return CourseTag.class;
    }

    @Override
    protected Class<CourseTagDto> getDtoClass() {
        return CourseTagDto.class;
    }

    @Override
    public void deleteByCourseId(Long courseId) {
        List<Long> ids = ((CourseTagRepository) repository).findByCourseIdAndIsDeletedFalse(courseId)
                .stream().map(CourseTag::getId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.deleteByIds(ids);
        }
    }
}
