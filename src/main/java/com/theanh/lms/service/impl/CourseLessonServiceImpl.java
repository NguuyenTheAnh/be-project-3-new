package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.CourseLessonDto;
import com.theanh.lms.entity.CourseLesson;
import com.theanh.lms.repository.CourseLessonRepository;
import com.theanh.lms.service.CourseLessonService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseLessonServiceImpl extends BaseServiceImpl<CourseLesson, CourseLessonDto, Long> implements CourseLessonService {

    private final CourseLessonRepository courseLessonRepository;

    public CourseLessonServiceImpl(CourseLessonRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.courseLessonRepository = repository;
    }

    @Override
    public void deleteByCourseId(Long courseId) {
        List<Long> ids = courseLessonRepository.findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .filter(cl -> !Boolean.TRUE.equals(cl.getIsDeleted()))
                .map(CourseLesson::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.deleteByIds(ids);
        }
    }

    @Override
    public void deleteBySectionId(Long courseSectionId) {
        List<Long> ids = courseLessonRepository.findByCourseSectionIdOrderByPositionAsc(courseSectionId)
                .stream()
                .filter(cl -> !Boolean.TRUE.equals(cl.getIsDeleted()))
                .map(CourseLesson::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.deleteByIds(ids);
        }
    }

    @Override
    public void deleteByCourseAndLesson(Long courseId, Long lessonId) {
        List<Long> ids = courseLessonRepository.findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .filter(cl -> !Boolean.TRUE.equals(cl.getIsDeleted()))
                .filter(cl -> cl.getLessonId().equals(lessonId))
                .map(CourseLesson::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.deleteByIds(ids);
        }
    }

    @Override
    public List<CourseLessonDto> findByCourseId(Long courseId) {
        return courseLessonRepository.findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .filter(cl -> !Boolean.TRUE.equals(cl.getIsDeleted()))
                .map(cl -> modelMapper.map(cl, CourseLessonDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public CourseLessonDto findActiveByLessonId(Long lessonId) {
        return courseLessonRepository.findActiveByLessonId(lessonId)
                .map(cl -> modelMapper.map(cl, CourseLessonDto.class))
                .orElse(null);
    }

    @Override
    public long countActiveByCourseId(Long courseId) {
        return courseLessonRepository.countActiveByCourseId(courseId);
    }

    @Override
    protected Class<CourseLesson> getEntityClass() {
        return CourseLesson.class;
    }

    @Override
    protected Class<CourseLessonDto> getDtoClass() {
        return CourseLessonDto.class;
    }
}
