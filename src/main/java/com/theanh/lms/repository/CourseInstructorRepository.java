package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.CourseInstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseInstructorRepository extends BaseRepository<CourseInstructor, Long> {

    List<CourseInstructor> findByCourseId(Long courseId);
}
