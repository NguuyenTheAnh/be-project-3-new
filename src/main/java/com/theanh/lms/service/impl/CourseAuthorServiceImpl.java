package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.CourseDetailResponse;
import com.theanh.lms.dto.request.*;
import com.theanh.lms.entity.*;
import com.theanh.lms.repository.*;
import com.theanh.lms.service.CourseAuthorService;
import com.theanh.lms.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseAuthorServiceImpl implements CourseAuthorService {

    private final CourseRepository courseRepository;
    private final CourseTagRepository courseTagRepository;
    private final TagRepository tagRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final LessonRepository lessonRepository;
    private final CatalogService catalogService;

    private static final Set<String> ALLOWED_STATUS = Set.of("DRAFT", "REVIEW", "PUBLISHED", "ARCHIVED");

    @Override
    @Transactional
    public CourseDetailResponse createCourse(Long creatorUserId, CourseCreateRequest request) {
        Course course = Course.builder()
                .creatorUserId(creatorUserId)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .slug(request.getSlug())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .level(request.getLevel())
                .language(request.getLanguage())
                .thumbnailFileId(request.getThumbnailFileId())
                .introVideoFileId(request.getIntroVideoFileId())
                .status("DRAFT")
                .ratingAvg(null)
                .ratingCount(0)
                .build();
        course.setIsActive(Boolean.TRUE);
        course.setIsDeleted(Boolean.FALSE);
        course = courseRepository.save(course);
        syncTags(course.getId(), request.getTagIds());
        if (!CollectionUtils.isEmpty(request.getInstructors())) {
            syncInstructors(course.getId(), request.getInstructors());
        } else {
            // Ensure creator is owner
            CourseInstructor owner = CourseInstructor.builder()
                    .courseId(course.getId())
                    .userId(creatorUserId)
                    .instructorRole("OWNER")
                    .build();
            courseInstructorRepository.save(owner);
        }
        return catalogService.getCourseDetail(course.getId(), null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateCourse(Long courseId, CourseUpdateRequest request) {
        Course course = getCourseOrThrow(courseId);
        if (StringUtils.hasText(request.getTitle())) {
            course.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getSlug())) {
            course.setSlug(request.getSlug());
        }
        if (request.getCategoryId() != null) {
            course.setCategoryId(request.getCategoryId());
        }
        if (request.getShortDescription() != null) {
            course.setShortDescription(request.getShortDescription());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getLevel())) {
            course.setLevel(request.getLevel());
        }
        if (StringUtils.hasText(request.getLanguage())) {
            course.setLanguage(request.getLanguage());
        }
        if (request.getThumbnailFileId() != null) {
            course.setThumbnailFileId(request.getThumbnailFileId());
        }
        if (request.getIntroVideoFileId() != null) {
            course.setIntroVideoFileId(request.getIntroVideoFileId());
        }
        courseRepository.save(course);
        if (request.getTagIds() != null) {
            syncTags(courseId, request.getTagIds());
        }
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateStatus(Long courseId, CourseStatusUpdateRequest request) {
        Course course = getCourseOrThrow(courseId);
        String target = request.getStatus();
        if (!ALLOWED_STATUS.contains(target)) {
            throw new BusinessException("data.fail");
        }
        course.setStatus(target);
        courseRepository.save(course);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateTags(Long courseId, List<Long> tagIds) {
        getCourseOrThrow(courseId);
        syncTags(courseId, tagIds);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateInstructors(Long courseId, List<CourseInstructorRequest> instructors) {
        getCourseOrThrow(courseId);
        syncInstructors(courseId, instructors);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse addSection(Long courseId, CourseSectionRequest request) {
        getCourseOrThrow(courseId);
        CourseSection section = CourseSection.builder()
                .courseId(courseId)
                .title(request.getTitle())
                .position(request.getPosition())
                .build();
        section.setIsActive(Boolean.TRUE);
        section.setIsDeleted(Boolean.FALSE);
        courseSectionRepository.save(section);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateSection(Long courseId, Long sectionId, CourseSectionRequest request) {
        getCourseOrThrow(courseId);
        CourseSection section = courseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!courseId.equals(section.getCourseId())) {
            throw new BusinessException("data.fail");
        }
        if (StringUtils.hasText(request.getTitle())) {
            section.setTitle(request.getTitle());
        }
        if (request.getPosition() != null) {
            section.setPosition(request.getPosition());
        }
        courseSectionRepository.save(section);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse deleteSection(Long courseId, Long sectionId) {
        getCourseOrThrow(courseId);
        CourseSection section = courseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!courseId.equals(section.getCourseId())) {
            throw new BusinessException("data.fail");
        }
        courseSectionRepository.delete(section);
        // also delete courseLesson referencing this section
        List<CourseLesson> toDelete = courseLessonRepository.findByCourseSectionIdOrderByPositionAsc(sectionId);
        courseLessonRepository.deleteAll(toDelete);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse addLesson(Long courseId, LessonCreateRequest request) {
        getCourseOrThrow(courseId);
        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .lessonType(request.getLessonType())
                .contentText(request.getContentText())
                .videoFileId(request.getVideoFileId())
                .durationSeconds(request.getDurationSeconds())
                .isFreePreview(request.getIsFreePreview())
                .build();
        lesson.setIsActive(Boolean.TRUE);
        lesson.setIsDeleted(Boolean.FALSE);
        lesson = lessonRepository.save(lesson);

        CourseLesson mapping = CourseLesson.builder()
                .courseId(courseId)
                .courseSectionId(request.getCourseSectionId())
                .lessonId(lesson.getId())
                .position(request.getPosition())
                .isPreview(request.getIsPreview())
                .build();
        mapping.setIsActive(Boolean.TRUE);
        mapping.setIsDeleted(Boolean.FALSE);
        courseLessonRepository.save(mapping);

        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateLesson(Long courseId, Long lessonId, LessonUpdateRequest request) {
        getCourseOrThrow(courseId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (StringUtils.hasText(request.getTitle())) {
            lesson.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getLessonType())) {
            lesson.setLessonType(request.getLessonType());
        }
        if (request.getContentText() != null) {
            lesson.setContentText(request.getContentText());
        }
        if (request.getVideoFileId() != null) {
            lesson.setVideoFileId(request.getVideoFileId());
        }
        if (request.getDurationSeconds() != null) {
            lesson.setDurationSeconds(request.getDurationSeconds());
        }
        if (request.getIsFreePreview() != null) {
            lesson.setIsFreePreview(request.getIsFreePreview());
        }
        lessonRepository.save(lesson);

        List<CourseLesson> mappings = courseLessonRepository.findByCourseIdOrderByPositionAsc(courseId)
                .stream().filter(cl -> cl.getLessonId().equals(lessonId)).toList();
        for (CourseLesson cl : mappings) {
            if (request.getCourseSectionId() != null) {
                cl.setCourseSectionId(request.getCourseSectionId());
            }
            if (request.getPosition() != null) {
                cl.setPosition(request.getPosition());
            }
            if (request.getIsPreview() != null) {
                cl.setIsPreview(request.getIsPreview());
            }
        }
        courseLessonRepository.saveAll(mappings);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse deleteLesson(Long courseId, Long lessonId) {
        getCourseOrThrow(courseId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        courseLessonRepository.deleteAll(
                courseLessonRepository.findByCourseIdOrderByPositionAsc(courseId).stream()
                        .filter(cl -> cl.getLessonId().equals(lessonId))
                        .toList()
        );
        lessonRepository.delete(lesson);
        return catalogService.getCourseDetail(courseId, null);
    }

    private void syncTags(Long courseId, List<Long> tagIds) {
        courseTagRepository.deleteAll(courseTagRepository.findByCourseId(courseId));
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        Set<Long> valid = new HashSet<>(tagRepository.findAllById(tagIds).stream().map(Tag::getId).toList());
        for (Long tagId : valid) {
            CourseTag ct = CourseTag.builder()
                    .courseId(courseId)
                    .tagId(tagId)
                    .build();
            ct.setIsActive(Boolean.TRUE);
            ct.setIsDeleted(Boolean.FALSE);
            courseTagRepository.save(ct);
        }
    }

    private void syncInstructors(Long courseId, List<CourseInstructorRequest> instructors) {
        courseInstructorRepository.deleteAll(courseInstructorRepository.findByCourseId(courseId));
        if (CollectionUtils.isEmpty(instructors)) {
            return;
        }
        for (CourseInstructorRequest req : instructors) {
            CourseInstructor ci = CourseInstructor.builder()
                    .courseId(courseId)
                    .userId(req.getUserId())
                    .instructorRole(req.getInstructorRole())
                    .revenueShare(req.getRevenueShare())
                    .build();
            ci.setIsActive(Boolean.TRUE);
            ci.setIsDeleted(Boolean.FALSE);
            courseInstructorRepository.save(ci);
        }
    }

    private Course getCourseOrThrow(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (Boolean.TRUE.equals(course.getIsDeleted())) {
            throw new BusinessException("data.not_found");
        }
        return course;
    }
}
