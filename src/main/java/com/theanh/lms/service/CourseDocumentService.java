package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.CourseDocumentDto;
import com.theanh.lms.entity.CourseDocument;

import java.util.List;

public interface CourseDocumentService extends BaseService<CourseDocument, CourseDocumentDto, Long> {

    List<CourseDocumentDto> findByCourseId(Long courseId);

    void deleteByCourseId(Long courseId);

    CourseDocumentDto findActiveById(Long id);
}
