package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.CourseDetailResponse;
import com.theanh.lms.dto.CourseInstructorDto;
import com.theanh.lms.dto.CourseLessonDto;
import com.theanh.lms.dto.CourseDocumentDto;
import com.theanh.lms.dto.CourseSectionDto;
import com.theanh.lms.dto.CourseTagDto;
import com.theanh.lms.dto.LessonDto;
import com.theanh.lms.dto.LessonDocumentDto;
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
import com.theanh.lms.repository.CourseDocumentRepository;
import com.theanh.lms.repository.LessonDocumentRepository;
import com.theanh.lms.repository.LessonRepository;
import com.theanh.lms.service.CourseInstructorService;
import com.theanh.lms.service.CourseLessonService;
import com.theanh.lms.service.CourseAuthorService;
import com.theanh.lms.service.CourseSectionService;
import com.theanh.lms.service.CourseTagService;
import com.theanh.lms.service.CatalogService;
import com.theanh.lms.service.CourseDocumentService;
import com.theanh.lms.service.LessonService;
import com.theanh.lms.service.LessonDocumentService;
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
    private final CourseDocumentService courseDocumentService;
    private final LessonDocumentService lessonDocumentService;
    private final LessonService lessonService;
    private final LessonRepository lessonRepository;
    private final CourseDocumentRepository courseDocumentRepository;
    private final LessonDocumentRepository lessonDocumentRepository;
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
                .priceCents(request.getPriceCents() != null && request.getPriceCents() >= 0 ? request.getPriceCents() : 0L)
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
        if (request.getPriceCents() != null && request.getPriceCents() >= 0) {
            course.setPriceCents(request.getPriceCents());
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
        LessonDto lesson = lessonService.findById(lessonId); // ensure exists
        Long videoFileId = lesson != null ? lesson.getVideoFileId() : null;
        courseLessonService.deleteByCourseAndLesson(courseId, lessonId);
        lessonService.deleteById(lessonId);
        if (videoFileId != null && isFileFree(videoFileId)) {
            uploadedFileService.markReadyIfAttached(videoFileId);
        }
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse reorderSections(Long courseId, ReorderRequest request) {
        getCourseOrThrow(courseId);
        if (CollectionUtils.isEmpty(request.getItems())) {
            throw new BusinessException("data.fail");
        }
        List<CourseSectionDto> sections = courseSectionService.findByCourseId(courseId);
        Set<Long> validIds = sections.stream().map(CourseSectionDto::getId).collect(Collectors.toSet());
        Map<Long, Integer> desired = request.getItems().stream()
                .collect(Collectors.toMap(ReorderItemRequest::getId, ReorderItemRequest::getPosition, (a, b) -> b));
        if (!validIds.containsAll(desired.keySet())) {
            throw new BusinessException("data.fail");
        }
        for (CourseSectionDto section : sections) {
            if (desired.containsKey(section.getId())) {
                section.setPosition(desired.get(section.getId()));
                courseSectionService.saveObject(section);
            }
        }
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse reorderLessons(Long courseId, ReorderRequest request) {
        getCourseOrThrow(courseId);
        if (CollectionUtils.isEmpty(request.getItems())) {
            throw new BusinessException("data.fail");
        }
        List<CourseLessonDto> mappings = courseLessonService.findByCourseId(courseId);
        Set<Long> validIds = mappings.stream().map(CourseLessonDto::getId).collect(Collectors.toSet());
        Map<Long, Integer> desired = request.getItems().stream()
                .collect(Collectors.toMap(ReorderItemRequest::getId, ReorderItemRequest::getPosition, (a, b) -> b));
        if (!validIds.containsAll(desired.keySet())) {
            throw new BusinessException("data.fail");
        }
        for (CourseLessonDto mapping : mappings) {
            if (desired.containsKey(mapping.getId())) {
                mapping.setPosition(desired.get(mapping.getId()));
                courseLessonService.saveObject(mapping);
            }
        }
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse addCourseDocument(Long courseId, CourseDocumentRequest request) {
        getCourseOrThrow(courseId);
        uploadedFileService.markAttached(request.getUploadedFileId(), courseId, null, UploadPurpose.DOCUMENT);
        List<CourseDocumentDto> existing = courseDocumentService.findByCourseId(courseId);
        CourseDocumentDto dto = new CourseDocumentDto();
        dto.setCourseId(courseId);
        dto.setUploadedFileId(request.getUploadedFileId());
        dto.setTitle(request.getTitle());
        dto.setPosition(resolvePosition(request.getPosition(), existing.stream().map(CourseDocumentDto::getPosition).toList()));
        courseDocumentService.saveObject(dto);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateCourseDocument(Long courseId, Long documentId, CourseDocumentRequest request) {
        getCourseOrThrow(courseId);
        CourseDocumentDto doc = courseDocumentService.findActiveById(documentId);
        if (doc == null || !courseId.equals(doc.getCourseId())) {
            throw new BusinessException("data.not_found");
        }
        if (request.getUploadedFileId() != null) {
            uploadedFileService.markAttached(request.getUploadedFileId(), courseId, null, UploadPurpose.DOCUMENT);
            doc.setUploadedFileId(request.getUploadedFileId());
        }
        if (request.getTitle() != null) {
            doc.setTitle(request.getTitle());
        }
        if (request.getPosition() != null) {
            doc.setPosition(request.getPosition());
        }
        courseDocumentService.saveObject(doc);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse deleteCourseDocument(Long courseId, Long documentId) {
        getCourseOrThrow(courseId);
        CourseDocumentDto doc = courseDocumentService.findActiveById(documentId);
        if (doc == null || !courseId.equals(doc.getCourseId())) {
            throw new BusinessException("data.not_found");
        }
        courseDocumentService.deleteById(documentId);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse addLessonDocument(Long courseId, Long lessonId, LessonDocumentRequest request) {
        getCourseOrThrow(courseId);
        ensureLessonInCourse(courseId, lessonId);
        uploadedFileService.markAttached(request.getUploadedFileId(), courseId, lessonId, UploadPurpose.DOCUMENT);
        List<LessonDocumentDto> existing = lessonDocumentService.findByLessonId(lessonId);
        LessonDocumentDto dto = new LessonDocumentDto();
        dto.setLessonId(lessonId);
        dto.setUploadedFileId(request.getUploadedFileId());
        dto.setTitle(request.getTitle());
        dto.setPosition(resolvePosition(request.getPosition(), existing.stream().map(LessonDocumentDto::getPosition).toList()));
        lessonDocumentService.saveObject(dto);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse updateLessonDocument(Long courseId, Long lessonId, Long documentId, LessonDocumentRequest request) {
        getCourseOrThrow(courseId);
        ensureLessonInCourse(courseId, lessonId);
        LessonDocumentDto doc = lessonDocumentService.findActiveById(documentId);
        if (doc == null || !lessonId.equals(doc.getLessonId())) {
            throw new BusinessException("data.not_found");
        }
        if (request.getUploadedFileId() != null) {
            uploadedFileService.markAttached(request.getUploadedFileId(), courseId, lessonId, UploadPurpose.DOCUMENT);
            doc.setUploadedFileId(request.getUploadedFileId());
        }
        if (request.getTitle() != null) {
            doc.setTitle(request.getTitle());
        }
        if (request.getPosition() != null) {
            doc.setPosition(request.getPosition());
        }
        lessonDocumentService.saveObject(doc);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse deleteLessonDocument(Long courseId, Long lessonId, Long documentId) {
        getCourseOrThrow(courseId);
        ensureLessonInCourse(courseId, lessonId);
        LessonDocumentDto doc = lessonDocumentService.findActiveById(documentId);
        if (doc == null || !lessonId.equals(doc.getLessonId())) {
            throw new BusinessException("data.not_found");
        }
        lessonDocumentService.deleteById(documentId);
        return catalogService.getCourseDetail(courseId, null);
    }

    @Override
    @Transactional
    public CourseDetailResponse publishCourse(Long courseId) {
        Course course = getCourseOrThrow(courseId);
        String target = CourseStatus.PUBLISHED.name();
        if (!isTransitionAllowed(course.getStatus(), target)) {
            throw new BusinessException("data.fail");
        }
        course.setStatus(target);
        courseRepository.save(course);
        return catalogService.getCourseDetail(courseId, null);
    }

    private void ensureLessonInCourse(Long courseId, Long lessonId) {
        LessonDto lesson = lessonService.findById(lessonId);
        if (lesson == null) {
            throw new BusinessException("data.not_found");
        }
        boolean belongsToCourse = courseLessonService.findByCourseId(courseId).stream()
                .anyMatch(cl -> lessonId.equals(cl.getLessonId()));
        if (!belongsToCourse) {
            throw new BusinessException("data.fail");
        }
    }

    private Integer resolvePosition(Integer requested, List<Integer> existingPositions) {
        if (requested != null) {
            return requested;
        }
        int max = existingPositions.stream()
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        return max + 1;
    }

    private boolean isFileFree(Long fileId) {
        long lessonUses = lessonRepository.countByVideoFileIdAndIsDeletedFalse(fileId);
        long courseIntroUses = courseRepository.countByIntroVideoFileIdAndIsDeletedFalse(fileId);
        long courseThumbUses = courseRepository.countByThumbnailFileIdAndIsDeletedFalse(fileId);
        long courseDocUses = courseDocumentRepository.countByUploadedFileIdAndIsDeletedFalse(fileId);
        long lessonDocUses = lessonDocumentRepository.countByUploadedFileIdAndIsDeletedFalse(fileId);
        return (lessonUses + courseIntroUses + courseThumbUses + courseDocUses + lessonDocUses) == 0;
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
