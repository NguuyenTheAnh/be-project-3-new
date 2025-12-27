package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.config.StorageProperties;
import com.theanh.lms.constants.MessageCode;
import com.theanh.lms.enums.StorageProvider;
import com.theanh.lms.model.StoredFile;
import com.theanh.lms.service.FileStorageService;
import com.theanh.lms.utils.ChecksumUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {
        Path baseDir = storageProperties.resolvedBasePath();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            log.error("Could not initialize storage directory: {}", baseDir, e);
            throw new BusinessException(MessageCode.STORAGE_INIT_FAILED);
        }
    }

    @Override
    public StoredFile store(MultipartFile file, boolean isPublic) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(MessageCode.FILE_EMPTY);
        }
        String originalName = StringUtils.cleanPath(StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename()
                : "file");
        String extension = StringUtils.getFilenameExtension(originalName);
        String objectKey = UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.hasText(extension)) {
            objectKey = objectKey + "." + extension;
        }

        byte[] bytes = toBytes(file);
        Path targetPath = storageProperties.resolvedBasePath().resolve(objectKey);
        try {
            Files.write(targetPath, bytes, StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException e) {
            log.warn("Object key collision detected, regenerating key");
            return store(file, isPublic);
        } catch (IOException e) {
            log.error("Failed to store file {}", originalName, e);
            throw new BusinessException(MessageCode.FILE_UPLOAD_FAILED);
        }

        String checksum = ChecksumUtils.sha256(bytes);
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";

        return StoredFile.builder()
                .storageProvider(StorageProvider.LOCAL.name())
                .bucket(null)
                .objectKey(objectKey)
                .originalName(originalName)
                .contentType(contentType)
                .sizeBytes((long) bytes.length)
                .checksumSha256(checksum)
                .isPublic(isPublic)
                .build();
    }

    @Override
    public Resource loadAsResource(String objectKey) {
        Path path = storageProperties.resolvedBasePath().resolve(objectKey);
        if (!Files.exists(path)) {
            throw new BusinessException(MessageCode.FILE_NOT_FOUND);
        }
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException e) {
            log.error("Invalid object key {}", objectKey, e);
        }
        throw new BusinessException(MessageCode.FILE_NOT_FOUND);
    }

    @Override
    public String buildPublicUrl(String objectKey) {
        String baseUrl = storageProperties.buildPublicBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return null;
        }
        return baseUrl + objectKey;
    }

    private byte[] toBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.error("Unable to read bytes from uploaded file", e);
            throw new BusinessException(MessageCode.FILE_UPLOAD_FAILED);
        }
    }
}
