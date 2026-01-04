package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.EnrollmentDto;
import com.theanh.lms.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService extends BaseService<Enrollment, EnrollmentDto, Long> {

    EnrollmentDto enroll(Long userId, Long courseId);

    Page<EnrollmentDto> findByUser(Long userId, Pageable pageable);

    boolean isEnrolled(Long userId, Long courseId);
}
