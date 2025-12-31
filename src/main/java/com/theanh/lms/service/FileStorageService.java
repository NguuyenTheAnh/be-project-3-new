package com.theanh.lms.service;

import com.theanh.lms.model.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile store(MultipartFile file, boolean isPublic);

    Resource loadAsResource(String objectKey);

    String buildPublicUrl(String objectKey);

    StoredFile getMetadata(String objectKey);

    void delete(String objectKey);
}
