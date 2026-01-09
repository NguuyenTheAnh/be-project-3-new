package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.dto.CourseLessonDto;
import com.theanh.lms.dto.LessonDto;
import com.theanh.lms.service.AccessControlService;
import com.theanh.lms.service.CourseLessonService;
import com.theanh.lms.service.CourseService;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.LessonService;
import com.theanh.lms.service.UserService;
import com.theanh.lms.enums.RoleName;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Service
public class AccessControlServiceImpl implements AccessControlService {

    private final CourseLessonService courseLessonService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final UserService userService;

    public AccessControlServiceImpl(CourseLessonService courseLessonService,
            LessonService lessonService,
            EnrollmentService enrollmentService,
            CourseService courseService,
            UserService userService) {
        this.courseLessonService = courseLessonService;
        this.lessonService = lessonService;
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @Override
    public boolean canViewLesson(Long userId, Long courseId, Long lessonId) {
        LessonDto lesson = lessonService.findById(lessonId);
        if (lesson == null) {
            return false;
        }
        CourseLessonDto mapping = courseId != null ? null : courseLessonService.findActiveByLessonId(lessonId);
        Long resolvedCourseId = courseId != null ? courseId : (mapping != null ? mapping.getCourseId() : null);
        boolean isPreview = Boolean.TRUE.equals(lesson.getIsFreePreview())
                || Boolean.TRUE.equals(mapping != null ? mapping.getIsPreview() : null);
        if (isPreview) {
            return true;
        }
        if (userId == null) {
            return false;
        }
        if (resolvedCourseId == null) {
            return false;
        }
        if (isCourseCreator(userId, resolvedCourseId)) {
            return true;
        }
        return enrollmentService.isEnrolled(userId, resolvedCourseId);
    }

    @Override
    public void ensureFileViewable(UploadedFileDto file, Long userId) {
        if (file == null) {
            throw new BusinessException("data.not_found");
        }
        if (Boolean.TRUE.equals(file.getIsPublic())) {
            return;
        }
        if (userId != null && userService.findRoles(userId).contains(RoleName.ADMIN.name())) {
            return;
        }
        Long courseId = file.getCourseId();
        Long lessonId = file.getLessonId();
        // Private file without course/lesson context -> reject
        if (courseId == null && lessonId == null) {
            throw new BusinessException("data.fail");
        }
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
        var course = courseService.findActiveById(courseId);
        return course != null && Objects.equals(course.getCreatorUserId(), userId);
    }
}
