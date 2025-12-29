package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.CourseTag;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseTagRepository extends BaseRepository<CourseTag, Long> {

    List<CourseTag> findByCourseId(Long courseId);

    List<CourseTag> findByCourseIdIn(List<Long> courseIds);
}
