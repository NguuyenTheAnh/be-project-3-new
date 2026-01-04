package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.QuestionVoteDto;
import com.theanh.lms.entity.QuestionVote;
import com.theanh.lms.enums.VoteType;
import com.theanh.lms.repository.QuestionVoteRepository;
import com.theanh.lms.service.QuestionVoteService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class QuestionVoteServiceImpl extends BaseServiceImpl<QuestionVote, QuestionVoteDto, Long> implements QuestionVoteService {

    private final QuestionVoteRepository repository;

    public QuestionVoteServiceImpl(QuestionVoteRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public QuestionVoteDto upsertVote(Long userId, Long questionId, String voteType) {
        VoteType type;
        try {
            type = VoteType.valueOf(voteType);
        } catch (Exception ex) {
            throw new BusinessException("data.fail");
        }
        QuestionVote vote = repository.findActiveByQuestionAndUser(questionId, userId)
                .orElseGet(() -> {
                    QuestionVote v = new QuestionVote();
                    v.setQuestionId(questionId);
                    v.setUserId(userId);
                    v.setIsActive(Boolean.TRUE);
                    v.setIsDeleted(Boolean.FALSE);
                    return v;
                });
        vote.setVoteType(type.name());
        QuestionVote saved = repository.save(vote);
        return modelMapper.map(saved, QuestionVoteDto.class);
    }

    @Override
    protected Class<QuestionVote> getEntityClass() {
        return QuestionVote.class;
    }

    @Override
    protected Class<QuestionVoteDto> getDtoClass() {
        return QuestionVoteDto.class;
    }
}
