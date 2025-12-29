package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.CourseLesson;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseLessonRepository extends BaseRepository<CourseLesson, Long> {

    List<CourseLesson> findByCourseIdOrderByPositionAsc(Long courseId);

    List<CourseLesson> findByCourseSectionIdOrderByPositionAsc(Long courseSectionId);
}
