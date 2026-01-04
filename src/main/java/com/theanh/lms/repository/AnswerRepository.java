package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Answer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends BaseRepository<Answer, Long> {

    @Query(value = """
            SELECT * FROM answer a
            WHERE a.question_id = :questionId
              AND (a.is_deleted IS NULL OR a.is_deleted = 0)
            ORDER BY a.created_date ASC
            """, nativeQuery = true)
    List<Answer> findByQuestion(@Param("questionId") Long questionId);

    @Query(value = """
            SELECT * FROM answer a
            WHERE a.id = :id
              AND (a.is_deleted IS NULL OR a.is_deleted = 0)
            """, nativeQuery = true)
    Optional<Answer> findActiveById(@Param("id") Long id);
}
