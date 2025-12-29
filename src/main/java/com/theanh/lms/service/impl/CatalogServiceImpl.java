package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.*;
import com.theanh.lms.entity.*;
import com.theanh.lms.repository.*;
import com.theanh.lms.service.CatalogService;
import com.theanh.lms.service.CategoryService;
import com.theanh.lms.service.TagService;
import com.theanh.lms.service.UploadedFileService;
import com.theanh.lms.utils.UserViewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CategoryService categoryService;
    private final TagService tagService;
    private final CourseRepository courseRepository;
    private final CourseTagRepository courseTagRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final LessonRepository lessonRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final UserRepository userRepository;
    private final UploadedFileService uploadedFileService;
    private final UserViewMapper userViewMapper;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Override
    public List<CategoryDto> listCategories() {
        return categoryService.findAll();
    }

    @Override
    public List<TagDto> listTags() {
        return tagService.findAll();
    }

    @Override
    public Page<CourseListItemResponse> searchCourses(String keyword,
                                                      Long categoryId,
                                                      List<Long> tagIds,
                                                      String level,
                                                      String language,
                                                      String sort,
                                                      Pageable pageable) {
        Specification<Course> spec = notDeletedOrNull();
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "PUBLISHED"));
        if (StringUtils.hasText(keyword)) {
            String kw = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), kw),
                    cb.like(cb.lower(root.get("shortDescription")), kw)
            ));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryId"), categoryId));
        }
        if (StringUtils.hasText(level)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("level"), level));
        }
        if (StringUtils.hasText(language)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("language"), language));
        }
        if (!CollectionUtils.isEmpty(tagIds)) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                var sub = query.subquery(Long.class);
                var ct = sub.from(CourseTag.class);
                sub.select(ct.get("courseId")).where(cb.and(
                        cb.equal(ct.get("courseId"), root.get("id")),
                        ct.get("tagId").in(tagIds)
                ));
                return cb.exists(sub);
            });
        }

        Pageable sortedPageable = applySort(sort, pageable);
        Page<Course> page = courseRepository.findAll(spec, sortedPageable);

        List<CourseListItemResponse> content = mapCourseList(page.getContent());
        return new PageImpl<>(content, sortedPageable, page.getTotalElements());
    }

    @Override
    public CourseDetailResponse getCourseDetail(Long idOrNull, String slugOrNull) {
        Optional<Course> courseOpt;
        if (idOrNull != null) {
            courseOpt = courseRepository.findById(idOrNull);
        } else if (StringUtils.hasText(slugOrNull)) {
            courseOpt = courseRepository.findBySlug(slugOrNull);
        } else {
            throw new BusinessException("data.not_found");
        }
        Course course = courseOpt.orElseThrow(() -> new BusinessException("data.not_found"));
        if (Boolean.TRUE.equals(course.getIsDeleted())) {
            throw new BusinessException("data.not_found");
        }
        CourseDetailResponse response = new CourseDetailResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setSlug(course.getSlug());
        response.setShortDescription(course.getShortDescription());
        response.setDescription(course.getDescription());
        response.setLevel(course.getLevel());
        response.setLanguage(course.getLanguage());
        response.setStatus(course.getStatus());
        response.setPublishedAt(course.getPublishedAt());
        response.setRatingAvg(Optional.ofNullable(course.getRatingAvg()).orElse(BigDecimal.ZERO));
        response.setRatingCount(Optional.ofNullable(course.getRatingCount()).orElse(0));

        if (course.getCategoryId() != null) {
            categoryRepository.findById(course.getCategoryId()).ifPresent(cat -> {
                CategoryDto catDto = new CategoryDto();
                catDto.setId(cat.getId());
                catDto.setName(cat.getName());
                catDto.setSlug(cat.getSlug());
                catDto.setParentId(cat.getParentId());
                catDto.setDescription(cat.getDescription());
                catDto.setPosition(cat.getPosition());
                response.setCategory(catDto);
            });
        }

        response.setTags(resolveTagsForCourse(course.getId()));

        if (course.getThumbnailFileId() != null) {
            try {
                response.setThumbnail(uploadedFileService.findById(course.getThumbnailFileId()));
            } catch (BusinessException ignored) {
            }
        }
        if (course.getIntroVideoFileId() != null) {
            try {
                response.setIntroVideo(uploadedFileService.findById(course.getIntroVideoFileId()));
            } catch (BusinessException ignored) {
            }
        }

        response.setInstructors(buildInstructorSummaries(course.getId()));
        response.setSections(buildSections(course.getId()));
        return response;
    }

    private List<CourseListItemResponse> mapCourseList(List<Course> courses) {
        if (CollectionUtils.isEmpty(courses)) {
            return List.of();
        }
        List<Long> courseIds = courses.stream().map(Course::getId).toList();

        Map<Long, Category> categoryMap = categoryRepository.findAllById(
                courses.stream().map(Course::getCategoryId).filter(Objects::nonNull).toList()
        ).stream().collect(Collectors.toMap(Category::getId, c -> c));

        Map<Long, List<Tag>> tagMap = buildTagMap(courseIds);

        Map<Long, UploadedFileDto> thumbnailMap = new HashMap<>();
        courses.stream().map(Course::getThumbnailFileId).filter(Objects::nonNull).distinct().forEach(fid -> {
            try {
                thumbnailMap.put(fid, uploadedFileService.findById(fid));
            } catch (BusinessException ignored) {
            }
        });

        return courses.stream().map(course -> {
            CourseListItemResponse dto = new CourseListItemResponse();
            dto.setId(course.getId());
            dto.setTitle(course.getTitle());
            dto.setSlug(course.getSlug());
            dto.setShortDescription(course.getShortDescription());
            dto.setLevel(course.getLevel());
            dto.setLanguage(course.getLanguage());
            dto.setRatingAvg(Optional.ofNullable(course.getRatingAvg()).orElse(BigDecimal.ZERO));
            dto.setRatingCount(Optional.ofNullable(course.getRatingCount()).orElse(0));

            if (course.getCategoryId() != null) {
                Category cat = categoryMap.get(course.getCategoryId());
                if (cat != null) {
                    CategoryDto catDto = new CategoryDto();
                    catDto.setId(cat.getId());
                    catDto.setName(cat.getName());
                    catDto.setSlug(cat.getSlug());
                    catDto.setParentId(cat.getParentId());
                    catDto.setDescription(cat.getDescription());
                    catDto.setPosition(cat.getPosition());
                    dto.setCategory(catDto);
                }
            }

            List<Tag> tagList = tagMap.getOrDefault(course.getId(), List.of());
            dto.setTags(tagList.stream().map(t -> {
                TagDto td = new TagDto();
                td.setId(t.getId());
                td.setName(t.getName());
                td.setSlug(t.getSlug());
                return td;
            }).toList());

            if (course.getThumbnailFileId() != null) {
                dto.setThumbnail(thumbnailMap.get(course.getThumbnailFileId()));
            }
            return dto;
        }).toList();
    }

    private Map<Long, List<Tag>> buildTagMap(List<Long> courseIds) {
        if (CollectionUtils.isEmpty(courseIds)) {
            return Map.of();
        }
        List<CourseTag> mappings = courseTagRepository.findByCourseIdIn(courseIds);
        if (CollectionUtils.isEmpty(mappings)) {
            return Map.of();
        }
        Set<Long> tagIds = mappings.stream().map(CourseTag::getTagId).collect(Collectors.toSet());
        Map<Long, Tag> tagMap = tagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        Map<Long, List<Tag>> result = new HashMap<>();
        for (CourseTag ct : mappings) {
            Tag tag = tagMap.get(ct.getTagId());
            if (tag == null) {
                continue;
            }
            result.computeIfAbsent(ct.getCourseId(), k -> new ArrayList<>()).add(tag);
        }
        return result;
    }

    private List<TagDto> resolveTagsForCourse(Long courseId) {
        List<CourseTag> mappings = courseTagRepository.findByCourseId(courseId);
        if (CollectionUtils.isEmpty(mappings)) {
            return List.of();
        }
        List<Long> tagIds = mappings.stream().map(CourseTag::getTagId).toList();
        return tagRepository.findAllById(tagIds).stream()
                .map(t -> {
                    TagDto dto = new TagDto();
                    dto.setId(t.getId());
                    dto.setName(t.getName());
                    dto.setSlug(t.getSlug());
                    return dto;
                })
                .toList();
    }

    private List<InstructorSummaryDto> buildInstructorSummaries(Long courseId) {
        List<CourseInstructor> instructors = courseInstructorRepository.findByCourseId(courseId);
        if (CollectionUtils.isEmpty(instructors)) {
            return List.of();
        }
        Set<Long> userIds = instructors.stream().map(CourseInstructor::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, UploadedFileDto> avatarMap = new HashMap<>();
        userMap.values().stream()
                .map(User::getAvatarFileId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(fid -> {
                    try {
                        avatarMap.put(fid, uploadedFileService.findById(fid));
                    } catch (BusinessException ignored) {
                    }
                });

        return instructors.stream()
                .map(ins -> {
                    User u = userMap.get(ins.getUserId());
                    if (u == null) {
                        return null;
                    }
                    InstructorSummaryDto dto = new InstructorSummaryDto();
                    dto.setId(u.getId());
                    dto.setFullName(u.getFullName());
                    if (u.getAvatarFileId() != null) {
                        dto.setAvatarFile(avatarMap.get(u.getAvatarFileId()));
                    }
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<CourseSectionResponse> buildSections(Long courseId) {
        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByPositionAsc(courseId);
        List<CourseLesson> courseLessons = courseLessonRepository.findByCourseIdOrderByPositionAsc(courseId);
        if (CollectionUtils.isEmpty(courseLessons)) {
            courseLessons = List.of();
        }
        Map<Long, List<CourseLesson>> lessonBySection = courseLessons.stream()
                .filter(cl -> cl.getCourseSectionId() != null)
                .collect(Collectors.groupingBy(CourseLesson::getCourseSectionId));

        Set<Long> lessonIds = courseLessons.stream().map(CourseLesson::getLessonId).collect(Collectors.toSet());
        Map<Long, Lesson> lessonMap = lessonRepository.findAllById(lessonIds).stream()
                .filter(l -> !Boolean.TRUE.equals(l.getIsDeleted()))
                .collect(Collectors.toMap(Lesson::getId, l -> l));

        List<CourseSectionResponse> responses = new ArrayList<>();
        for (CourseSection section : sections) {
            CourseSectionResponse sr = new CourseSectionResponse();
            sr.setId(section.getId());
            sr.setTitle(section.getTitle());
            sr.setPosition(section.getPosition());
            List<CourseLesson> cls = lessonBySection.getOrDefault(section.getId(), List.of());
            sr.setLessons(buildLessonPreviews(cls, lessonMap));
            responses.add(sr);
        }
        // orphan lessons not in section
        List<CourseLesson> orphan = courseLessons.stream()
                .filter(cl -> cl.getCourseSectionId() == null)
                .toList();
        if (!orphan.isEmpty()) {
            CourseSectionResponse pseudo = new CourseSectionResponse();
            pseudo.setId(0L);
            pseudo.setTitle("Ungrouped");
            pseudo.setPosition(Integer.MAX_VALUE);
            pseudo.setLessons(buildLessonPreviews(orphan, lessonMap));
            responses.add(pseudo);
        }
        responses.sort(Comparator.comparing(cs -> Optional.ofNullable(cs.getPosition()).orElse(Integer.MAX_VALUE)));
        return responses;
    }

    private List<LessonPreviewDto> buildLessonPreviews(List<CourseLesson> cls, Map<Long, Lesson> lessonMap) {
        if (CollectionUtils.isEmpty(cls)) {
            return List.of();
        }
        return cls.stream()
                .sorted(Comparator.comparing(cl -> Optional.ofNullable(cl.getPosition()).orElse(Integer.MAX_VALUE)))
                .map(cl -> {
                    Lesson lesson = lessonMap.get(cl.getLessonId());
                    if (lesson == null) {
                        return null;
                    }
                    LessonPreviewDto dto = new LessonPreviewDto();
                    dto.setId(lesson.getId());
                    dto.setTitle(lesson.getTitle());
                    dto.setLessonType(lesson.getLessonType());
                    dto.setDurationSeconds(lesson.getDurationSeconds());
                    boolean preview = Boolean.TRUE.equals(lesson.getIsFreePreview()) || Boolean.TRUE.equals(cl.getIsPreview());
                    dto.setIsPreview(preview);
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Pageable applySort(String sort, Pageable pageable) {
        if (!StringUtils.hasText(sort)) {
            return pageable;
        }
        String s = sort.toLowerCase();
        Sort sortSpec = switch (s) {
            case "newest" ->
                    Sort.by(Sort.Direction.DESC, "publishedAt", "createdDate");
            case "rating" ->
                    Sort.by(Sort.Direction.DESC, "ratingAvg", "ratingCount");
            case "popular" ->
                    Sort.by(Sort.Direction.DESC, "ratingCount", "ratingAvg");
            default -> pageable.getSort();
        };
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortSpec);
    }

    private Specification<Course> notDeletedOrNull() {
        return (root, query, cb) -> cb.or(
                cb.isFalse(root.get("isDeleted")),
                cb.isNull(root.get("isDeleted"))
        );
    }
}
