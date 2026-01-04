package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CourseRepository extends BaseRepository<Course, Long> {

    Optional<Course> findBySlug(String slug);

    @Query(value = """
            SELECT c.*
            FROM course c
            WHERE (c.is_deleted IS NULL OR c.is_deleted = 0)
              AND c.status = 'PUBLISHED'
              AND (:keyword IS NULL OR LOWER(c.title) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(c.short_description) LIKE CONCAT('%', LOWER(:keyword), '%'))
              AND (:categoryId IS NULL OR c.category_id = :categoryId)
              AND (:level IS NULL OR c.level = :level)
              AND (:language IS NULL OR c.language = :language)
              AND (:tagCount = 0 OR EXISTS (
                    SELECT 1 FROM course_tag ct
                    WHERE ct.course_id = c.id
                      AND (ct.is_deleted IS NULL OR ct.is_deleted = 0)
                      AND ct.tag_id IN (:tagIds)
              ))
            """,
            countQuery = """
            SELECT COUNT(1)
            FROM course c
            WHERE (c.is_deleted IS NULL OR c.is_deleted = 0)
              AND c.status = 'PUBLISHED'
              AND (:keyword IS NULL OR LOWER(c.title) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(c.short_description) LIKE CONCAT('%', LOWER(:keyword), '%'))
              AND (:categoryId IS NULL OR c.category_id = :categoryId)
              AND (:level IS NULL OR c.level = :level)
              AND (:language IS NULL OR c.language = :language)
              AND (:tagCount = 0 OR EXISTS (
                    SELECT 1 FROM course_tag ct
                    WHERE ct.course_id = c.id
                      AND (ct.is_deleted IS NULL OR ct.is_deleted = 0)
                      AND ct.tag_id IN (:tagIds)
              ))
            """,
            nativeQuery = true)
    Page<Course> searchPublishedCourses(@Param("keyword") String keyword,
                                        @Param("categoryId") Long categoryId,
                                        @Param("level") String level,
                                        @Param("language") String language,
                                        @Param("tagIds") List<Long> tagIds,
                                        @Param("tagCount") int tagCount,
                                        Pageable pageable);

    long countByIntroVideoFileIdAndIsDeletedFalse(Long fileId);

    long countByThumbnailFileIdAndIsDeletedFalse(Long fileId);
}
