package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Progress;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressRepository extends BaseRepository<Progress, Long> {

    Optional<Progress> findByEnrollmentIdAndLessonIdAndIsDeletedFalse(Long enrollmentId, Long lessonId);

    @Query(value = """
            SELECT COUNT(1)
            FROM progress p
            WHERE p.enrollment_id = :enrollmentId
              AND (p.is_deleted IS NULL OR p.is_deleted = 0)
              AND p.completed = 1
            """, nativeQuery = true)
    long countCompletedLessons(@Param("enrollmentId") Long enrollmentId);

    @Query(value = """
            SELECT p.lesson_id
            FROM progress p
            JOIN course_lesson cl
              ON cl.lesson_id = p.lesson_id
             AND cl.course_id = :courseId
            WHERE p.enrollment_id = :enrollmentId
              AND (p.is_deleted IS NULL OR p.is_deleted = 0)
              AND p.completed = 1
              AND (cl.is_deleted IS NULL OR cl.is_deleted = 0)
            ORDER BY cl.position ASC
            """, nativeQuery = true)
    java.util.List<Long> findCompletedLessonIds(@Param("enrollmentId") Long enrollmentId,
                                                @Param("courseId") Long courseId);
}
