package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Lesson;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends BaseRepository<Lesson, Long> {

    long countByVideoFileIdAndIsDeletedFalse(Long videoFileId);
}
