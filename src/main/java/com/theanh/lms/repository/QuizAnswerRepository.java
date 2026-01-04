package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.QuizAnswer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends BaseRepository<QuizAnswer, Long> {

    @Query(value = """
            SELECT * FROM quiz_answer qa
            WHERE qa.question_id = :questionId
              AND (qa.is_deleted IS NULL OR qa.is_deleted = 0)
            ORDER BY COALESCE(qa.position, 999999)
            """, nativeQuery = true)
    List<QuizAnswer> findActiveByQuestion(@Param("questionId") Long questionId);
}
