package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.CourseLessonDto;
import com.theanh.lms.entity.CourseLesson;

public interface CourseLessonService extends BaseService<CourseLesson, CourseLessonDto, Long> {

    void deleteByCourseId(Long courseId);

    void deleteBySectionId(Long courseSectionId);

    void deleteByCourseAndLesson(Long courseId, Long lessonId);

    java.util.List<CourseLessonDto> findByCourseId(Long courseId);

    CourseLessonDto findActiveByLessonId(Long lessonId);
}
