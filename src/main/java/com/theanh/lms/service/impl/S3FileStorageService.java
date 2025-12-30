package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.config.S3StorageProperties;
import com.theanh.lms.config.StorageProperties;
import com.theanh.lms.constants.MessageCode;
import com.theanh.lms.enums.StorageProvider;
import com.theanh.lms.model.StoredFile;
import com.theanh.lms.service.FileStorageService;
import com.theanh.lms.utils.ChecksumUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "file.storage.provider", havingValue = "MINIO")
public class S3FileStorageService implements FileStorageService {

    private final S3StorageProperties s3Props;
    private final StorageProperties storageProperties;
    private S3Client s3Client;

    @PostConstruct
    public void init() {
        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(s3Props.isPathStyleAccess())
                .build();

        S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Props.getAccessKey(), s3Props.getSecretKey())))
                .region(Region.of(s3Props.getRegion()))
                .serviceConfiguration(s3Config);
        if (StringUtils.hasText(s3Props.getEndpoint())) {
            builder = builder.endpointOverride(URI.create(s3Props.getEndpoint()));
        }
        s3Client = builder.build();
    }

    @Override
    public StoredFile store(MultipartFile file, boolean isPublic) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(MessageCode.FILE_EMPTY);
        }
        String originalName = StringUtils.cleanPath(StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename()
                : "file");
        String objectKey = buildObjectKey(originalName, file.getContentType());
        byte[] bytes = toBytes(file);
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(s3Props.getBucket())
                .key(objectKey)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .acl(isPublic ? "public-read" : null)
                .build();
        s3Client.putObject(putReq, RequestBody.fromBytes(bytes));

        String checksum = ChecksumUtils.sha256(bytes);
        return StoredFile.builder()
                .storageProvider(StorageProvider.MINIO.name())
                .bucket(s3Props.getBucket())
                .objectKey(objectKey)
                .originalName(originalName)
                .contentType(contentType)
                .sizeBytes((long) bytes.length)
                .checksumSha256(checksum)
                .isPublic(isPublic)
                .build();
    }

    private String buildObjectKey(String originalName, String contentType) {
        String ext = StringUtils.getFilenameExtension(originalName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String folder = resolveFolder(contentType);
        StringBuilder key = new StringBuilder(folder);
        if (!folder.endsWith("/")) {
            key.append('/');
        }
        key.append(uuid);
        if (StringUtils.hasText(ext)) {
            key.append('.').append(ext);
        }
        return key.toString();
    }

    private String resolveFolder(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "other";
        }
        String lower = contentType.toLowerCase();
        if (lower.startsWith("image/")) {
            return "images";
        }
        if (lower.startsWith("video/")) {
            return "videos";
        }
        return "other";
    }

    @Override
    public Resource loadAsResource(String objectKey) {
        try {
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(s3Props.getBucket())
                    .key(objectKey)
                    .build();
            byte[] data = s3Client.getObjectAsBytes(getReq).asByteArray();
            return new ByteArrayResource(data);
        } catch (Exception e) {
            log.error("Failed to read object {} from bucket {}", objectKey, s3Props.getBucket(), e);
            throw new BusinessException(MessageCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public String buildPublicUrl(String objectKey) {
        String baseUrl = s3Props.getPublicBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = storageProperties.buildPublicBaseUrl();
        }
        if (!StringUtils.hasText(baseUrl)) {
            return null;
        }
        return baseUrl.endsWith("/") ? baseUrl + objectKey : baseUrl + "/" + objectKey;
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
