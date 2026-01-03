package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.CourseDocumentDto;
import com.theanh.lms.entity.CourseDocument;
import com.theanh.lms.repository.CourseDocumentRepository;
import com.theanh.lms.service.CourseDocumentService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseDocumentServiceImpl extends BaseServiceImpl<CourseDocument, CourseDocumentDto, Long> implements CourseDocumentService {

    private final CourseDocumentRepository repository;

    public CourseDocumentServiceImpl(CourseDocumentRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public List<CourseDocumentDto> findByCourseId(Long courseId) {
        return repository.findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                .map(doc -> modelMapper.map(doc, CourseDocumentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByCourseId(Long courseId) {
        List<Long> ids = repository.findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                .map(CourseDocument::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.deleteByIds(ids);
        }
    }

    @Override
    public CourseDocumentDto findActiveById(Long id) {
        return repository.findById(id)
                .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                .map(doc -> modelMapper.map(doc, CourseDocumentDto.class))
                .orElse(null);
    }

    @Override
    protected Class<CourseDocument> getEntityClass() {
        return CourseDocument.class;
    }

    @Override
    protected Class<CourseDocumentDto> getDtoClass() {
        return CourseDocumentDto.class;
    }
}
