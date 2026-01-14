package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.CourseReviewAdminResponse;
import com.theanh.lms.dto.CourseReviewDto;
import com.theanh.lms.dto.CourseDto;
import com.theanh.lms.dto.UserDto;
import com.theanh.lms.entity.Course;
import com.theanh.lms.entity.CourseReview;
import com.theanh.lms.enums.ReviewStatus;
import com.theanh.lms.repository.CourseRepository;
import com.theanh.lms.repository.CourseReviewRepository;
import com.theanh.lms.service.CourseReviewService;
import com.theanh.lms.service.CourseService;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CourseReviewServiceImpl extends BaseServiceImpl<CourseReview, CourseReviewDto, Long> implements CourseReviewService {

    private final CourseReviewRepository repository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final UserService userService;

    public CourseReviewServiceImpl(CourseReviewRepository repository,
                                   CourseRepository courseRepository,
                                   EnrollmentService enrollmentService,
                                   CourseService courseService,
                                   UserService userService,
                                   ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
        this.courseRepository = courseRepository;
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public CourseReviewDto upsertReview(Long userId, Long courseId, Integer rating, String title, String content) {
        validateCourseAndEnrollment(userId, courseId);
        CourseReview review = repository.findActiveByCourseAndUser(courseId, userId)
                .orElseGet(() -> {
                    CourseReview r = new CourseReview();
                    r.setCourseId(courseId);
                    r.setUserId(userId);
                    r.setIsActive(Boolean.TRUE);
                    r.setIsDeleted(Boolean.FALSE);
                    return r;
                });
        if (rating != null) {
            review.setRating(rating);
        }
        if (title != null) {
            review.setTitle(title);
        }
        if (content != null) {
            review.setContent(content);
        }
        review.setStatus(ReviewStatus.PENDING.name());
        review.setModeratedByUserId(null);
        review.setModeratedAt(null);
        CourseReview saved = repository.save(review);
        return modelMapper.map(saved, CourseReviewDto.class);
    }

    @Override
    public Page<CourseReviewDto> listApproved(Long courseId, Pageable pageable) {
        return repository.findApprovedByCourse(courseId, pageable)
                .map(r -> modelMapper.map(r, CourseReviewDto.class));
    }

    @Override
    public CourseReviewDto getMyReview(Long userId, Long courseId) {
        return repository.findActiveByCourseAndUser(courseId, userId)
                .map(r -> modelMapper.map(r, CourseReviewDto.class))
                .orElse(null);
    }

    @Override
    @Transactional
    public CourseReviewDto moderate(Long reviewId, Long moderatorId, String status) {
        ReviewStatus target = parseStatus(status);
        CourseReview review = repository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (Boolean.TRUE.equals(review.getIsDeleted())) {
            throw new BusinessException("data.not_found");
        }
        review.setStatus(target.name());
        review.setModeratedByUserId(moderatorId);
        review.setModeratedAt(LocalDateTime.now());
        CourseReview saved = repository.save(review);
        refreshCourseRating(review.getCourseId());
        return modelMapper.map(saved, CourseReviewDto.class);
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long courseId, Long reviewId) {
        CourseReview review = repository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!review.getUserId().equals(userId) || !review.getCourseId().equals(courseId)) {
            throw new BusinessException("data.fail");
        }
        repository.deleteById(reviewId);
        refreshCourseRating(courseId);
    }

    @Override
    public Page<CourseReviewAdminResponse> listForAdmin(Long courseId, String status, Pageable pageable) {
        String normalizedStatus = null;
        if (StringUtils.hasText(status)) {
            normalizedStatus = parseStatus(status).name();
        }
        Page<CourseReview> page = repository.findForAdmin(courseId, normalizedStatus, pageable);
        List<CourseReview> reviews = page.getContent();
        if (reviews.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, page.getTotalElements());
        }
        Map<Long, CourseDto> courseMap = courseService.findByIds(reviews.stream()
                        .map(CourseReview::getCourseId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(CourseDto::getId, Function.identity()));
        Map<Long, UserDto> userMap = userService.findByIds(reviews.stream()
                        .map(CourseReview::getUserId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        List<CourseReviewAdminResponse> responses = reviews.stream()
                .map(review -> {
                    CourseReviewAdminResponse resp = modelMapper.map(review, CourseReviewAdminResponse.class);
                    CourseDto course = courseMap.get(review.getCourseId());
                    UserDto user = userMap.get(review.getUserId());
                    if (course != null) {
                        resp.setCourseTitle(course.getTitle());
                    }
                    if (user != null) {
                        String name = user.getFullName();
                        if (!StringUtils.hasText(name)) {
                            name = user.getEmail();
                        }
                        resp.setStudentName(name);
                    }
                    return resp;
                })
                .toList();
        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    private void refreshCourseRating(Long courseId) {
        Object statsObj = repository.computeRatingStats(courseId);
        if (statsObj instanceof Object[]) {
            Object[] arr = (Object[]) statsObj;
            BigDecimal avg = toBigDecimal(arr[0]);
            Integer count = toInteger(arr[1]);
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            courseOpt.ifPresent(course -> {
                course.setRatingAvg(avg);
                course.setRatingCount(count);
                courseRepository.save(course);
            });
        }
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        try {
            return new BigDecimal(o.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Integer toInteger(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private void validateCourseAndEnrollment(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!StringUtils.hasText(course.getStatus()) || !"PUBLISHED".equals(course.getStatus())) {
            throw new BusinessException("data.fail");
        }
        if (!enrollmentService.isEnrolled(userId, courseId)) {
            throw new BusinessException("data.fail");
        }
    }

    private ReviewStatus parseStatus(String raw) {
        try {
            return ReviewStatus.valueOf(raw);
        } catch (Exception ex) {
            throw new BusinessException("data.fail");
        }
    }

    @Override
    protected Class<CourseReview> getEntityClass() {
        return CourseReview.class;
    }

    @Override
    protected Class<CourseReviewDto> getDtoClass() {
        return CourseReviewDto.class;
    }
}
