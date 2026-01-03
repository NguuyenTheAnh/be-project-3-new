package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.LessonDocumentDto;
import com.theanh.lms.entity.LessonDocument;

import java.util.List;

public interface LessonDocumentService extends BaseService<LessonDocument, LessonDocumentDto, Long> {

    List<LessonDocumentDto> findByLessonId(Long lessonId);

    void deleteByLessonId(Long lessonId);

    LessonDocumentDto findActiveById(Long id);
}
