package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.QuestionVoteDto;
import com.theanh.lms.entity.QuestionVote;

public interface QuestionVoteService extends BaseService<QuestionVote, QuestionVoteDto, Long> {

    QuestionVoteDto upsertVote(Long userId, Long questionId, String voteType);
}
