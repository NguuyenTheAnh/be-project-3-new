package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.CourseSection;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSectionRepository extends BaseRepository<CourseSection, Long> {

    List<CourseSection> findByCourseIdOrderByPositionAsc(Long courseId);
}
