package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.EnrollmentDto;
import com.theanh.lms.entity.Course;
import com.theanh.lms.entity.Enrollment;
import com.theanh.lms.enums.CourseStatus;
import com.theanh.lms.repository.CourseRepository;
import com.theanh.lms.repository.EnrollmentRepository;
import com.theanh.lms.service.EnrollmentService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class EnrollmentServiceImpl extends BaseServiceImpl<Enrollment, EnrollmentDto, Long> implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentServiceImpl(EnrollmentRepository repository,
                                 CourseRepository courseRepository,
                                 ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.enrollmentRepository = repository;
        this.courseRepository = courseRepository;
    }

    @Override
    public EnrollmentDto enroll(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!StringUtils.hasText(course.getStatus()) || !CourseStatus.PUBLISHED.name().equals(course.getStatus())) {
            throw new BusinessException("data.fail");
        }

        Enrollment existing = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElse(null);
        if (existing != null) {
            if (Boolean.TRUE.equals(existing.getIsDeleted())) {
                existing.setIsDeleted(Boolean.FALSE);
                existing.setStatus("ACTIVE");
                if (existing.getEnrolledAt() == null) {
                    existing.setEnrolledAt(LocalDateTime.now());
                }
                Enrollment saved = enrollmentRepository.save(existing);
                return modelMapper.map(saved, EnrollmentDto.class);
            }
            return modelMapper.map(existing, EnrollmentDto.class);
        }

        Enrollment enrollment = Enrollment.builder()
                .courseId(courseId)
                .userId(userId)
                .enrolledAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();
        enrollment.setIsActive(Boolean.TRUE);
        enrollment.setIsDeleted(Boolean.FALSE);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return modelMapper.map(saved, EnrollmentDto.class);
    }

    @Override
    public Page<EnrollmentDto> findByUser(Long userId, Pageable pageable) {
        return enrollmentRepository.findByUserIdAndIsDeletedFalse(userId, pageable)
                .map(entity -> modelMapper.map(entity, EnrollmentDto.class));
    }

    @Override
    public boolean isEnrolled(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseIdAndIsDeletedFalse(userId, courseId);
    }

    @Override
    protected Class<Enrollment> getEntityClass() {
        return Enrollment.class;
    }

    @Override
    protected Class<EnrollmentDto> getDtoClass() {
        return EnrollmentDto.class;
    }
}
