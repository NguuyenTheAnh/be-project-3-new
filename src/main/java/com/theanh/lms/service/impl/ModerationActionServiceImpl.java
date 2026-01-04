package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.ModerationActionDto;
import com.theanh.lms.entity.ModerationAction;
import com.theanh.lms.repository.ModerationActionRepository;
import com.theanh.lms.service.ModerationActionService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ModerationActionServiceImpl extends BaseServiceImpl<ModerationAction, ModerationActionDto, Long> implements ModerationActionService {

    public ModerationActionServiceImpl(ModerationActionRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    protected Class<ModerationAction> getEntityClass() {
        return ModerationAction.class;
    }

    @Override
    protected Class<ModerationActionDto> getDtoClass() {
        return ModerationActionDto.class;
    }
}
