package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.CourseReviewAdminResponse;
import com.theanh.lms.dto.CourseReviewDto;
import com.theanh.lms.entity.CourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseReviewService extends BaseService<CourseReview, CourseReviewDto, Long> {

    CourseReviewDto upsertReview(Long userId, Long courseId, Integer rating, String title, String content);

    Page<CourseReviewDto> listApproved(Long courseId, Pageable pageable);

    CourseReviewDto getMyReview(Long userId, Long courseId);

    CourseReviewDto moderate(Long reviewId, Long moderatorId, String status);

    void deleteReview(Long userId, Long courseId, Long reviewId);

    Page<CourseReviewAdminResponse> listForAdmin(Long courseId, String status, Pageable pageable);
}
