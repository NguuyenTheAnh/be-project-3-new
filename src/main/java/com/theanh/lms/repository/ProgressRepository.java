package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Progress;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressRepository extends BaseRepository<Progress, Long> {

    Optional<Progress> findByEnrollmentIdAndLessonIdAndIsDeletedFalse(Long enrollmentId, Long lessonId);
}
