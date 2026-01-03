package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.CourseDocument;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseDocumentRepository extends BaseRepository<CourseDocument, Long> {

    List<CourseDocument> findByCourseIdOrderByPositionAsc(Long courseId);
}
