package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.LessonDocument;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonDocumentRepository extends BaseRepository<LessonDocument, Long> {

    List<LessonDocument> findByLessonIdOrderByPositionAsc(Long lessonId);

    long countByUploadedFileIdAndIsDeletedFalse(Long fileId);
}
