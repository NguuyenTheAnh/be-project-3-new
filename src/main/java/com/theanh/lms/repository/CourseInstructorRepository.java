package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.CourseInstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseInstructorRepository extends BaseRepository<CourseInstructor, Long> {

    List<CourseInstructor> findByCourseId(Long courseId);

    List<CourseInstructor> findByCourseIdAndIsDeletedFalse(Long courseId);

    @Query(value = """
            SELECT COUNT(1)
            FROM course_instructor ci
            WHERE ci.course_id = :courseId
              AND ci.user_id = :userId
              AND (ci.is_deleted IS NULL OR ci.is_deleted = 0)
            """, nativeQuery = true)
    long countActiveByCourseAndUser(@Param("courseId") Long courseId, @Param("userId") Long userId);
}
