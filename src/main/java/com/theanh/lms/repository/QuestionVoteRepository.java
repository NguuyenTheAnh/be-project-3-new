package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.QuestionVote;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionVoteRepository extends BaseRepository<QuestionVote, Long> {

    @Query(value = """
            SELECT * FROM question_vote qv
            WHERE qv.question_id = :questionId
              AND qv.user_id = :userId
              AND (qv.is_deleted IS NULL OR qv.is_deleted = 0)
            """, nativeQuery = true)
    Optional<QuestionVote> findActiveByQuestionAndUser(@Param("questionId") Long questionId,
                                                       @Param("userId") Long userId);
}
