package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.entity.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

public interface UploadedFileService extends BaseService<UploadedFile, UploadedFileDto, Long> {

    UploadedFileDto store(MultipartFile file, boolean isPublic);

    UploadedFileDto getByObjectKey(String objectKey);
}
