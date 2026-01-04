package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.CourseReviewDto;
import com.theanh.lms.dto.request.CourseReviewRequest;
import com.theanh.lms.dto.request.ReviewModerationRequest;
import com.theanh.lms.service.CourseReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CourseReviewController {

    private final CourseReviewService courseReviewService;

    @PostMapping("/courses/{courseId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<CourseReviewDto>> createOrUpdateReview(@PathVariable @NotNull Long courseId,
                                                                             @RequestBody @Valid CourseReviewRequest request) {
        Long userId = currentUserId();
        CourseReviewDto dto = courseReviewService.upsertReview(userId, courseId,
                request.getRating(), request.getTitle(), request.getContent());
        return ResponseConfig.success(dto);
    }

    @GetMapping("/courses/{courseId}/reviews")
    public ResponseEntity<ResponseDto<Page<CourseReviewDto>>> listReviews(@PathVariable @NotNull Long courseId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseConfig.success(courseReviewService.listApproved(courseId, pageable));
    }

    @GetMapping("/courses/{courseId}/reviews/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<CourseReviewDto>> myReview(@PathVariable @NotNull Long courseId) {
        Long userId = currentUserId();
        return ResponseConfig.success(courseReviewService.getMyReview(userId, courseId));
    }

    @DeleteMapping("/courses/{courseId}/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<Void>> deleteMyReview(@PathVariable @NotNull Long courseId,
                                                            @PathVariable @NotNull Long reviewId) {
        Long userId = currentUserId();
        courseReviewService.deleteReview(userId, courseId, reviewId);
        return ResponseConfig.success(null);
    }

    @PatchMapping("/admin/reviews/{reviewId}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<CourseReviewDto>> moderate(@PathVariable @NotNull Long reviewId,
                                                                 @RequestBody @Valid ReviewModerationRequest request) {
        Long moderatorId = currentUserId();
        return ResponseConfig.success(courseReviewService.moderate(reviewId, moderatorId, request.getStatus()));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getPrincipal().toString());
    }
}
