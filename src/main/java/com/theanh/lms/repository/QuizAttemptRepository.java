package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.QuizAttempt;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends BaseRepository<QuizAttempt, Long> {

    @Query(value = """
            SELECT * FROM quiz_attempt qa
            WHERE qa.quiz_id = :quizId
              AND qa.user_id = :userId
              AND (qa.is_deleted IS NULL OR qa.is_deleted = 0)
            ORDER BY qa.created_date DESC
            """, nativeQuery = true)
    List<QuizAttempt> findByUserAndQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);

    @Query(value = """
            SELECT * FROM quiz_attempt qa
            WHERE qa.id = :id
              AND (qa.is_deleted IS NULL OR qa.is_deleted = 0)
            """, nativeQuery = true)
    Optional<QuizAttempt> findActiveById(@Param("id") Long id);
}
