package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.dto.CourseLessonDto;
import com.theanh.lms.dto.LessonDto;
import com.theanh.lms.entity.Course;
import com.theanh.lms.repository.CourseRepository;
import com.theanh.lms.service.AccessControlService;
import com.theanh.lms.service.CourseLessonService;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.LessonService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Service
public class AccessControlServiceImpl implements AccessControlService {

    private final CourseLessonService courseLessonService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final CourseRepository courseRepository;

    public AccessControlServiceImpl(CourseLessonService courseLessonService,
                                    LessonService lessonService,
                                    EnrollmentService enrollmentService,
                                    CourseRepository courseRepository) {
        this.courseLessonService = courseLessonService;
        this.lessonService = lessonService;
        this.enrollmentService = enrollmentService;
        this.courseRepository = courseRepository;
    }

    @Override
    public boolean canViewLesson(Long userId, Long courseId, Long lessonId) {
        LessonDto lesson = lessonService.findById(lessonId);
        if (lesson == null) {
            return false;
        }
        boolean isPreview = Boolean.TRUE.equals(lesson.getIsFreePreview());
        if (!Boolean.TRUE.equals(isPreview) && courseId != null) {
            List<CourseLessonDto> mappings = courseLessonService.findByCourseId(courseId);
            if (!CollectionUtils.isEmpty(mappings)) {
                isPreview = mappings.stream()
                        .filter(m -> Objects.equals(lessonId, m.getLessonId()))
                        .anyMatch(m -> Boolean.TRUE.equals(m.getIsPreview()));
            }
        }
        if (isPreview) {
            return true;
        }
        if (userId == null) {
            return false;
        }
        if (courseId != null) {
            // Course creator always allowed
            Course course = courseRepository.findById(courseId)
                    .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                    .orElse(null);
            if (course != null && Objects.equals(course.getCreatorUserId(), userId)) {
                return true;
            }
        }
        return enrollmentService.isEnrolled(userId, courseId);
    }

    @Override
    public void ensureFileViewable(UploadedFileDto file, Long userId) {
        if (file == null) {
            throw new BusinessException("data.not_found");
        }
        if (Boolean.TRUE.equals(file.getIsPublic())) {
            return;
        }
        Long courseId = file.getCourseId();
        Long lessonId = file.getLessonId();
        boolean allowed;
        if (lessonId != null) {
            allowed = canViewLesson(userId, courseId, lessonId);
        } else if (courseId != null) {
            allowed = userId != null && (enrollmentService.isEnrolled(userId, courseId)
                    || isCourseCreator(userId, courseId));
        } else {
            allowed = userId != null; // generic private file: require auth
        }
        if (!allowed) {
            throw new BusinessException("data.fail");
        }
    }

    private boolean isCourseCreator(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            return false;
        }
        return courseRepository.findById(courseId)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .filter(c -> Objects.equals(c.getCreatorUserId(), userId))
                .isPresent();
    }
}
