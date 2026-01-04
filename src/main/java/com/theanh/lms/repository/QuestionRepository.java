package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends BaseRepository<Question, Long> {

    @Query(value = """
            SELECT * FROM question q
            WHERE q.id = :id
              AND (q.is_deleted IS NULL OR q.is_deleted = 0)
            """, nativeQuery = true)
    Optional<Question> findActiveById(@Param("id") Long id);

    @Query(value = """
            SELECT * FROM question q
            WHERE q.course_id = :courseId
              AND (q.is_deleted IS NULL OR q.is_deleted = 0)
            ORDER BY q.created_date DESC
            """,
            countQuery = """
            SELECT COUNT(1) FROM question q
            WHERE q.course_id = :courseId
              AND (q.is_deleted IS NULL OR q.is_deleted = 0)
            """,
            nativeQuery = true)
    Page<Question> findByCourse(@Param("courseId") Long courseId, Pageable pageable);
}
