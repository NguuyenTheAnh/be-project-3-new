package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.CourseProgressResponse;
import com.theanh.lms.dto.ProgressDto;
import com.theanh.lms.entity.Progress;

public interface ProgressService extends BaseService<Progress, ProgressDto, Long> {

    ProgressDto updateProgress(Long userId, Long courseId, Long lessonId, Integer lastPositionSeconds, Boolean completed);

    ProgressDto getProgress(Long userId, Long courseId, Long lessonId);

    CourseProgressResponse getCourseProgress(Long userId, Long courseId);

    java.util.List<com.theanh.lms.dto.LessonDto> getCompletedLessons(Long userId, Long courseId);
}
