package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.TagDto;
import com.theanh.lms.entity.Tag;
import com.theanh.lms.repository.TagRepository;
import com.theanh.lms.service.TagService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends BaseServiceImpl<Tag, TagDto, Long> implements TagService {

    public TagServiceImpl(TagRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    protected Class<Tag> getEntityClass() {
        return Tag.class;
    }

    @Override
    protected Class<TagDto> getDtoClass() {
        return TagDto.class;
    }
}
