package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.QuizQuestion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends BaseRepository<QuizQuestion, Long> {

    @Query(value = """
            SELECT * FROM quiz_question qq
            WHERE qq.quiz_id = :quizId
              AND (qq.is_deleted IS NULL OR qq.is_deleted = 0)
            ORDER BY COALESCE(qq.position, 999999)
            """, nativeQuery = true)
    List<QuizQuestion> findActiveByQuiz(@Param("quizId") Long quizId);
}
