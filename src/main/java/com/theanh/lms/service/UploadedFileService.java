package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.enums.UploadPurpose;
import com.theanh.lms.entity.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

public interface UploadedFileService extends BaseService<UploadedFile, UploadedFileDto, Long> {

    UploadedFileDto store(MultipartFile file, boolean isPublic);

    UploadedFileDto getByObjectKey(String objectKey);

    UploadedFileDto createPending(UploadPurpose purpose, String bucket, String objectKey, String filename,
                                  String contentType, Long size, Boolean isPublic, Long courseId, Long lessonId);

    UploadedFileDto markReady(Long fileId);

    UploadedFileDto abort(Long fileId);

    UploadedFileDto markAttached(Long fileId);

    UploadedFileDto markAttached(Long fileId, Long courseId, Long lessonId, UploadPurpose purpose);

    UploadedFile ensureReady(Long fileId);
}
