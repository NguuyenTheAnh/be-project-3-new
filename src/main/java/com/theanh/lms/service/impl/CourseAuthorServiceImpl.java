package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.CourseDetailResponse;
import com.theanh.lms.dto.CourseInstructorDto;
import com.theanh.lms.dto.CourseLessonDto;
import com.theanh.lms.dto.CourseSectionDto;
import com.theanh.lms.dto.CourseTagDto;
import com.theanh.lms.dto.LessonDto;
import com.theanh.lms.dto.request.*;
import com.theanh.lms.entity.*;
import com.theanh.lms.enums.CourseLevel;
import com.theanh.lms.enums.CourseLanguage;
import com.theanh.lms.enums.CourseStatus;
import com.theanh.lms.enums.InstructorRole;
import com.theanh.lms.enums.LessonType;
import com.theanh.lms.enums.UploadPurpose;
import com.theanh.lms.repository.CourseRepository;
import com.theanh.lms.repository.TagRepository;
import com.theanh.lms.service.CourseInstructorService;
import com.theanh.lms.service.CourseLessonService;
import com.theanh.lms.service.CourseAuthorService;
import com.theanh.lms.service.CourseSectionService;
import com.theanh.lms.service.CourseTagService;
import com.theanh.lms.service.CatalogService;
import com.theanh.lms.service.LessonService;
import com.theanh.lms.service.UploadedFileService;
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
    private final TagRepository tagRepository;
    private final CourseTagService courseTagService;
    private final CourseInstructorService courseInstructorService;
    private final CourseSectionService courseSectionService;
    private final CourseLessonService courseLessonService;
    private final LessonService lessonService;
    private final CatalogService catalogService;
    private final UploadedFileService uploadedFileService;

    private static final Set<String> ALLOWED_STATUS = Arrays.stream(CourseStatus.values())
            .map(Enum::name)
            .collect(Collectors.toSet());
    private static final Set<String> ALLOWED_LESSON_TYPES = Arrays.stream(LessonType.values())
            .map(Enum::name)
            .collect(Collectors.toSet());
    private static final Set<String> ALLOWED_LEVELS = Arrays.stream(CourseLevel.values())
            .map(Enum::name)
            .collect(Collectors.toSet());
    private static final Set<String> ALLOWED_LANGUAGES = Arrays.stream(CourseLanguage.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Override
    @Transactional
    public CourseDetailResponse createCourse(Long creatorUserId, CourseCreateRequest request) {
        validateLevelAndLanguage(request.getLevel(), request.getLanguage());
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
                .status(CourseStatus.DRAFT.name())
                .ratingAvg(null)
                .ratingCount(0)
                .build();
        course.setIsActive(Boolean.TRUE);
        course.setIsDeleted(Boolean.FALSE);
        course = courseRepository.save(course);
        if (request.getThumbnailFileId() != null) {
            uploadedFileService.markAttached(request.getThumbnailFileId(), course.getId(), null, UploadPurpose.THUMBNAIL);
        }
        if (request.getIntroVideoFileId() != null) {
            uploadedFileService.markAttached(request.getIntroVideoFileId(), course.getId(), null, UploadPurpose.INTRO_VIDEO);
        }
        syncTags(course.getId(), request.getTagIds());
        if (!CollectionUtils.isEmpty(request.getInstructors())) {
            syncInstructors(course.getId(), request.getInstructors());
        } else {
            // Ensure creator is owner
            CourseInstructorDto owner = new CourseInstructorDto();
            owner.setCourseId(course.getId());
            owner.setUserId(creatorUserId);
            owner.setInstructorRole(InstructorRole.OWNER.name());
            courseInstructorService.saveObject(owner);
        }
        return catalogService.getCourseDetail(course.getId(), null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateCourse(Long courseId, CourseUpdateRequest request) {
        Course course = getCourseOrThrow(courseId);
        validateLevelAndLanguage(request.getLevel(), request.getLanguage());
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
            uploadedFileService.markAttached(request.getThumbnailFileId(), courseId, null, UploadPurpose.THUMBNAIL);
        }
        if (request.getIntroVideoFileId() != null) {
            course.setIntroVideoFileId(request.getIntroVideoFileId());
            uploadedFileService.markAttached(request.getIntroVideoFileId(), courseId, null, UploadPurpose.INTRO_VIDEO);
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
        String current = course.getStatus();
        if (!isTransitionAllowed(current, target)) {
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
        CourseSectionDto section = new CourseSectionDto();
        section.setCourseId(courseId);
        section.setTitle(request.getTitle());
        section.setPosition(request.getPosition());
        courseSectionService.saveObject(section);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateSection(Long courseId, Long sectionId, CourseSectionRequest request) {
        getCourseOrThrow(courseId);
        CourseSectionDto section = courseSectionService.findById(sectionId);
        if (section == null) {
            throw new BusinessException("data.not_found");
        }
        if (!courseId.equals(section.getCourseId())) {
            throw new BusinessException("data.fail");
        }
        if (StringUtils.hasText(request.getTitle())) {
            section.setTitle(request.getTitle());
        }
        if (request.getPosition() != null) {
            section.setPosition(request.getPosition());
        }
        courseSectionService.saveObject(section);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse deleteSection(Long courseId, Long sectionId) {
        getCourseOrThrow(courseId);
        CourseSectionDto section = courseSectionService.findById(sectionId);
        if (section == null) {
            throw new BusinessException("data.not_found");
        }
        if (!courseId.equals(section.getCourseId())) {
            throw new BusinessException("data.fail");
        }
        courseSectionService.deleteById(sectionId);
        courseLessonService.deleteBySectionId(sectionId);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse addLesson(Long courseId, LessonCreateRequest request) {
        getCourseOrThrow(courseId);
        if (request.getLessonType() != null && !ALLOWED_LESSON_TYPES.contains(request.getLessonType())) {
            throw new BusinessException("data.fail");
        }
        LessonDto lesson = new LessonDto();
        lesson.setTitle(request.getTitle());
        lesson.setLessonType(request.getLessonType());
        lesson.setContentText(request.getContentText());
        lesson.setVideoFileId(request.getVideoFileId());
        lesson.setDurationSeconds(request.getDurationSeconds());
        lesson.setIsFreePreview(request.getIsFreePreview());
        LessonDto savedLesson = lessonService.saveObject(lesson);
        if (request.getVideoFileId() != null) {
            uploadedFileService.markAttached(request.getVideoFileId(), courseId, savedLesson.getId(), UploadPurpose.LESSON_VIDEO);
        }

        CourseLessonDto mapping = new CourseLessonDto();
        mapping.setCourseId(courseId);
        mapping.setCourseSectionId(request.getCourseSectionId());
        mapping.setLessonId(savedLesson.getId());
        mapping.setPosition(request.getPosition());
        mapping.setIsPreview(request.getIsPreview());
        courseLessonService.saveObject(mapping);

        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateLesson(Long courseId, Long lessonId, LessonUpdateRequest request) {
        getCourseOrThrow(courseId);
        LessonDto lesson = lessonService.findById(lessonId);
        if (lesson == null) {
            throw new BusinessException("data.not_found");
        }
        if (StringUtils.hasText(request.getTitle())) {
            lesson.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getLessonType())) {
            if (!ALLOWED_LESSON_TYPES.contains(request.getLessonType())) {
                throw new BusinessException("data.fail");
            }
            lesson.setLessonType(request.getLessonType());
        }
        if (request.getContentText() != null) {
            lesson.setContentText(request.getContentText());
        }
        if (request.getVideoFileId() != null) {
            lesson.setVideoFileId(request.getVideoFileId());
            uploadedFileService.markAttached(request.getVideoFileId(), courseId, lessonId, UploadPurpose.LESSON_VIDEO);
        }
        if (request.getDurationSeconds() != null) {
            lesson.setDurationSeconds(request.getDurationSeconds());
        }
        if (request.getIsFreePreview() != null) {
            lesson.setIsFreePreview(request.getIsFreePreview());
        }
        lessonService.saveObject(lesson);

        List<CourseLessonDto> mappings = courseLessonService.findByCourseId(courseId)
                .stream().filter(cl -> cl.getLessonId().equals(lessonId)).toList();
        for (CourseLessonDto cl : mappings) {
            if (request.getCourseSectionId() != null) {
                cl.setCourseSectionId(request.getCourseSectionId());
            }
            if (request.getPosition() != null) {
                cl.setPosition(request.getPosition());
            }
            if (request.getIsPreview() != null) {
                cl.setIsPreview(request.getIsPreview());
            }
            courseLessonService.saveObject(cl);
        }
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse deleteLesson(Long courseId, Long lessonId) {
        getCourseOrThrow(courseId);
        lessonService.findById(lessonId); // ensure exists
        courseLessonService.deleteByCourseAndLesson(courseId, lessonId);
        lessonService.deleteById(lessonId);
        return catalogService.getCourseDetail(courseId, null);
    }

    private void syncTags(Long courseId, List<Long> tagIds) {
        courseTagService.deleteByCourseId(courseId);
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        Set<Long> valid = new HashSet<>(tagRepository.findAllById(tagIds).stream().map(Tag::getId).toList());
        List<CourseTagDto> dtoList = valid.stream()
                .map(tagId -> {
                    CourseTagDto dto = new CourseTagDto();
                    dto.setCourseId(courseId);
                    dto.setTagId(tagId);
                    return dto;
                }).toList();
        courseTagService.saveListObject(dtoList);
    }

    private void syncInstructors(Long courseId, List<CourseInstructorRequest> instructors) {
        courseInstructorService.deleteByCourseId(courseId);
        if (CollectionUtils.isEmpty(instructors)) {
            return;
        }
        List<CourseInstructorDto> dtoList = instructors.stream().map(req -> {
            CourseInstructorDto dto = new CourseInstructorDto();
            dto.setCourseId(courseId);
            dto.setUserId(req.getUserId());
            dto.setInstructorRole(req.getInstructorRole());
            dto.setRevenueShare(req.getRevenueShare());
            return dto;
        }).toList();
        courseInstructorService.saveListObject(dtoList);
    }

    private Course getCourseOrThrow(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (Boolean.TRUE.equals(course.getIsDeleted())) {
            throw new BusinessException("data.not_found");
        }
        return course;
    }

    private boolean isTransitionAllowed(String current, String target) {
        if (target == null) {
            return false;
        }
        if (Objects.equals(current, target)) {
            return true;
        }
        // Allowed transitions:
        // DRAFT -> REVIEW/PUBLISHED/ARCHIVED
        // REVIEW -> PUBLISHED/DRAFT
        // PUBLISHED -> DRAFT/ARCHIVED
        // ARCHIVED -> (no further change)
        return switch (CourseStatus.valueOf(current)) {
            case DRAFT -> target.equals(CourseStatus.REVIEW.name())
                    || target.equals(CourseStatus.PUBLISHED.name())
                    || target.equals(CourseStatus.ARCHIVED.name());
            case REVIEW -> target.equals(CourseStatus.PUBLISHED.name())
                    || target.equals(CourseStatus.DRAFT.name());
            case PUBLISHED -> target.equals(CourseStatus.DRAFT.name())
                    || target.equals(CourseStatus.ARCHIVED.name());
            case ARCHIVED -> false;
        };
    }

    private void validateLevelAndLanguage(String level, String language) {
        if (level != null && StringUtils.hasText(level) && !ALLOWED_LEVELS.contains(level)) {
            throw new BusinessException("data.fail");
        }
        if (language != null && StringUtils.hasText(language) && !ALLOWED_LANGUAGES.contains(language)) {
            throw new BusinessException("data.fail");
        }
    }
}
