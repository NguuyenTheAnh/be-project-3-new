package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.CourseLesson;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseLessonRepository extends BaseRepository<CourseLesson, Long> {

    List<CourseLesson> findByCourseIdOrderByPositionAsc(Long courseId);

    List<CourseLesson> findByCourseSectionIdOrderByPositionAsc(Long courseSectionId);

    @Query(value = """
            SELECT * FROM course_lesson cl
            WHERE cl.lesson_id = :lessonId
              AND (cl.is_deleted IS NULL OR cl.is_deleted = 0)
            LIMIT 1
            """, nativeQuery = true)
    java.util.Optional<CourseLesson> findActiveByLessonId(@Param("lessonId") Long lessonId);
}
