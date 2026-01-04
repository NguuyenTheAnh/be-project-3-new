package com.theanh.lms.service;

import com.theanh.lms.dto.UploadedFileDto;

public interface AccessControlService {

    boolean canViewLesson(Long userId, Long courseId, Long lessonId);

    void ensureFileViewable(UploadedFileDto file, Long userId);
}
