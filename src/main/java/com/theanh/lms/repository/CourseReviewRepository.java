package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.CourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseReviewRepository extends BaseRepository<CourseReview, Long> {

    @Query(value = """
            SELECT * FROM course_review cr
            WHERE cr.course_id = :courseId
              AND cr.user_id = :userId
              AND (cr.is_deleted IS NULL OR cr.is_deleted = 0)
            """, nativeQuery = true)
    Optional<CourseReview> findActiveByCourseAndUser(@Param("courseId") Long courseId,
                                                    @Param("userId") Long userId);

    @Query(value = """
            SELECT * FROM course_review cr
            WHERE cr.course_id = :courseId
              AND cr.status = 'APPROVED'
              AND (cr.is_deleted IS NULL OR cr.is_deleted = 0)
            ORDER BY cr.created_date DESC
            """,
            countQuery = """
            SELECT COUNT(1) FROM course_review cr
            WHERE cr.course_id = :courseId
              AND cr.status = 'APPROVED'
              AND (cr.is_deleted IS NULL OR cr.is_deleted = 0)
            """,
            nativeQuery = true)
    Page<CourseReview> findApprovedByCourse(@Param("courseId") Long courseId, Pageable pageable);

    @Query(value = """
            SELECT IFNULL(AVG(cr.rating),0) as avg_rating,
                   COUNT(1) as rating_count
            FROM course_review cr
            WHERE cr.course_id = :courseId
              AND cr.status = 'APPROVED'
              AND (cr.is_deleted IS NULL OR cr.is_deleted = 0)
            """, nativeQuery = true)
    Object computeRatingStats(@Param("courseId") Long courseId);
}
