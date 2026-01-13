package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.CourseLessonDto;
import com.theanh.lms.dto.CourseProgressResponse;
import com.theanh.lms.dto.EnrollmentDto;
import com.theanh.lms.dto.LessonDto;
import com.theanh.lms.dto.ProgressDto;
import com.theanh.lms.repository.ProgressRepository;
import com.theanh.lms.entity.Progress;
import com.theanh.lms.service.AccessControlService;
import com.theanh.lms.service.CourseLessonService;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.LessonService;
import com.theanh.lms.service.ProgressService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ProgressServiceImpl extends BaseServiceImpl<Progress, ProgressDto, Long> implements ProgressService {

    private final ProgressRepository progressRepository;
    private final EnrollmentService enrollmentService;
    private final CourseLessonService courseLessonService;
    private final AccessControlService accessControlService;
    private final LessonService lessonService;

    public ProgressServiceImpl(ProgressRepository repository,
                               EnrollmentService enrollmentService,
                               CourseLessonService courseLessonService,
                               AccessControlService accessControlService,
                               LessonService lessonService,
                               ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.progressRepository = repository;
        this.enrollmentService = enrollmentService;
        this.courseLessonService = courseLessonService;
        this.accessControlService = accessControlService;
        this.lessonService = lessonService;
    }

    @Override
    public ProgressDto updateProgress(Long userId, Long courseId, Long lessonId, Integer lastPositionSeconds, Boolean completed) {
        if (!accessControlService.canViewLesson(userId, courseId, lessonId)) {
            throw new BusinessException("data.fail");
        }
        ensureLessonInCourse(courseId, lessonId);
        EnrollmentDto enrollment = enrollmentService.getActiveEnrollment(userId, courseId);
        if (enrollment == null) {
            throw new BusinessException("data.fail");
        }
        Progress progress = progressRepository.findByEnrollmentIdAndLessonIdAndIsDeletedFalse(enrollment.getId(), lessonId)
                .orElseGet(() -> {
                    Progress p = new Progress();
                    p.setEnrollmentId(enrollment.getId());
                    p.setLessonId(lessonId);
                    p.setIsActive(Boolean.TRUE);
                    p.setIsDeleted(Boolean.FALSE);
                    p.setCompleted(Boolean.FALSE);
                    return p;
                });
        if (lastPositionSeconds != null) {
            progress.setLastPositionSeconds(lastPositionSeconds);
        }
        progress.setLastAccessedAt(LocalDateTime.now());
        if (Boolean.TRUE.equals(completed)) {
            progress.setCompleted(Boolean.TRUE);
            progress.setCompletedAt(progress.getCompletedAt() == null ? LocalDateTime.now() : progress.getCompletedAt());
        }
        Progress saved = progressRepository.save(progress);
        return modelMapper.map(saved, ProgressDto.class);
    }

    @Override
    public ProgressDto getProgress(Long userId, Long courseId, Long lessonId) {
        if (!accessControlService.canViewLesson(userId, courseId, lessonId)) {
            throw new BusinessException("data.fail");
        }
        EnrollmentDto enrollment = enrollmentService.getActiveEnrollment(userId, courseId);
        if (enrollment == null) {
            throw new BusinessException("data.not_found");
        }
        return progressRepository.findByEnrollmentIdAndLessonIdAndIsDeletedFalse(enrollment.getId(), lessonId)
                .map(p -> modelMapper.map(p, ProgressDto.class))
                .orElse(null);
    }

    @Override
    public CourseProgressResponse getCourseProgress(Long userId, Long courseId) {
        EnrollmentDto enrollment = enrollmentService.getActiveEnrollment(userId, courseId);
        if (enrollment == null) {
            throw new BusinessException("data.not_found");
        }
        long totalLessons = courseLessonService.countActiveByCourseId(courseId);
        long completedLessons = progressRepository.countCompletedLessons(enrollment.getId());
        BigDecimal percent = BigDecimal.ZERO;
        if (totalLessons > 0) {
            percent = BigDecimal.valueOf(completedLessons)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);
        }
        CourseProgressResponse resp = new CourseProgressResponse();
        resp.setCourseId(courseId);
        resp.setTotalLessons(totalLessons);
        resp.setCompletedLessons(completedLessons);
        resp.setProgressPercent(percent);
        return resp;
    }

    @Override
    public List<LessonDto> getCompletedLessons(Long userId, Long courseId) {
        EnrollmentDto enrollment = enrollmentService.getActiveEnrollment(userId, courseId);
        if (enrollment == null) {
            throw new BusinessException("data.not_found");
        }
        List<Long> lessonIds = progressRepository.findCompletedLessonIds(enrollment.getId(), courseId);
        if (CollectionUtils.isEmpty(lessonIds)) {
            return List.of();
        }
        List<LessonDto> lessons = lessonService.findByIds(lessonIds);
        if (CollectionUtils.isEmpty(lessons)) {
            return List.of();
        }
        java.util.Map<Long, LessonDto> lessonMap = lessons.stream()
                .collect(java.util.stream.Collectors.toMap(LessonDto::getId, l -> l));
        return lessonIds.stream()
                .map(lessonMap::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private void ensureLessonInCourse(Long courseId, Long lessonId) {
        List<CourseLessonDto> mappings = courseLessonService.findByCourseId(courseId);
        if (CollectionUtils.isEmpty(mappings) || mappings.stream().noneMatch(m -> Objects.equals(m.getLessonId(), lessonId))) {
            throw new BusinessException("data.fail");
        }
    }

    @Override
    protected Class<Progress> getEntityClass() {
        return Progress.class;
    }

    @Override
    protected Class<ProgressDto> getDtoClass() {
        return ProgressDto.class;
    }
}
