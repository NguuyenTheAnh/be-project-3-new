package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends BaseRepository<Enrollment, Long> {

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    Page<Enrollment> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    boolean existsByUserIdAndCourseIdAndIsDeletedFalse(Long userId, Long courseId);

    Optional<Enrollment> findByUserIdAndCourseIdAndIsDeletedFalse(Long userId, Long courseId);
}
