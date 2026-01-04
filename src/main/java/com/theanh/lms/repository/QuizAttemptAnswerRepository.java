package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.QuizAttemptAnswer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptAnswerRepository extends BaseRepository<QuizAttemptAnswer, Long> {

    @Query(value = """
            SELECT * FROM quiz_attempt_answer qaa
            WHERE qaa.quiz_attempt_id = :attemptId
              AND (qaa.is_deleted IS NULL OR qaa.is_deleted = 0)
            """, nativeQuery = true)
    List<QuizAttemptAnswer> findByAttempt(@Param("attemptId") Long attemptId);
}
