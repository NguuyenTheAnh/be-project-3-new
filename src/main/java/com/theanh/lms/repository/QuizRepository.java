package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Quiz;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends BaseRepository<Quiz, Long> {

    @Query(value = """
            SELECT * FROM quiz q
            WHERE q.lesson_id = :lessonId
              AND (q.is_deleted IS NULL OR q.is_deleted = 0)
            """, nativeQuery = true)
    Optional<Quiz> findActiveByLesson(@Param("lessonId") Long lessonId);

    @Query(value = """
            SELECT * FROM quiz q
            WHERE q.id = :id
              AND (q.is_deleted IS NULL OR q.is_deleted = 0)
            """, nativeQuery = true)
    Optional<Quiz> findActiveById(@Param("id") Long id);
}
