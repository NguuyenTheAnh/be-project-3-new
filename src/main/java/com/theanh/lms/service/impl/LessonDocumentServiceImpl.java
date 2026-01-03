package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.LessonDocumentDto;
import com.theanh.lms.entity.LessonDocument;
import com.theanh.lms.repository.LessonDocumentRepository;
import com.theanh.lms.service.LessonDocumentService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonDocumentServiceImpl extends BaseServiceImpl<LessonDocument, LessonDocumentDto, Long> implements LessonDocumentService {

    private final LessonDocumentRepository repository;

    public LessonDocumentServiceImpl(LessonDocumentRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public List<LessonDocumentDto> findByLessonId(Long lessonId) {
        return repository.findByLessonIdOrderByPositionAsc(lessonId)
                .stream()
                .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                .map(doc -> modelMapper.map(doc, LessonDocumentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByLessonId(Long lessonId) {
        List<Long> ids = repository.findByLessonIdOrderByPositionAsc(lessonId)
                .stream()
                .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                .map(LessonDocument::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.deleteByIds(ids);
        }
    }

    @Override
    public LessonDocumentDto findActiveById(Long id) {
        return repository.findById(id)
                .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                .map(doc -> modelMapper.map(doc, LessonDocumentDto.class))
                .orElse(null);
    }

    @Override
    protected Class<LessonDocument> getEntityClass() {
        return LessonDocument.class;
    }

    @Override
    protected Class<LessonDocumentDto> getDtoClass() {
        return LessonDocumentDto.class;
    }
}
