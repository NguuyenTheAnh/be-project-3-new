package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.config.PresignProperties;
import com.theanh.lms.config.S3StorageProperties;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.dto.request.PresignPutRequest;
import com.theanh.lms.dto.response.PresignUrlResponse;
import com.theanh.lms.enums.UploadPurpose;
import com.theanh.lms.service.PresignService;
import com.theanh.lms.service.UploadedFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresignServiceImpl implements PresignService {

    private final S3StorageProperties s3Props;
    private final PresignProperties presignProperties;
    private final UploadedFileService uploadedFileService;

    @Override
    public PresignUrlResponse generatePutUrl(PresignPutRequest request) {
        UploadPurpose purpose = UploadPurpose.valueOf(request.getPurpose());
        String objectKey = buildObjectKey(purpose, request);

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(s3Props.getBucket())
                .key(objectKey)
                .contentType(request.getContentType())
                .build();

        S3Presigner presigner = presigner();
        Duration expiry = Duration.ofSeconds(presignProperties.getPutExpirySeconds());
        var presigned = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .putObjectRequest(put)
                        .signatureDuration(expiry)
                        .build()
        );
        Instant expiresAt = Instant.now().plusSeconds(presignProperties.getPutExpirySeconds());

        // Create metadata row in uploaded_file as PENDING
        UploadedFileDto meta = UploadedFileDto.builder()
                .storageProvider("MINIO")
                .bucket(s3Props.getBucket())
                .objectKey(objectKey)
                .originalName(request.getFilename())
                .contentType(request.getContentType())
                .isPublic(request.getIsPublic())
                .build();
        uploadedFileService.saveObject(meta);

        Map<String, String> headers = toSingleValueHeaders(presigned.signedHeaders());

        return PresignUrlResponse.builder()
                .url(presigned.url().toString())
                .method("PUT")
                .expiresAt(expiresAt)
                .bucket(s3Props.getBucket())
                .objectKey(objectKey)
                .headers(headers)
                .build();
    }

    @Override
    public PresignUrlResponse generateGetUrl(Long fileId) {
        UploadedFileDto file = uploadedFileService.findById(fileId);
        return generateGetUrl(file);
    }

    @Override
    public PresignUrlResponse generateGetUrl(UploadedFileDto file) {
        if (Boolean.TRUE.equals(file.getIsPublic()) && StringUtils.hasText(file.getAccessUrl())) {
            return PresignUrlResponse.builder()
                    .url(file.getAccessUrl())
                    .method("GET")
                    .expiresAt(null)
                    .bucket(file.getBucket())
                    .objectKey(file.getObjectKey())
                    .headers(Map.of())
                    .build();
        }
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(file.getBucket())
                .key(file.getObjectKey())
                .build();
        S3Presigner presigner = presigner();
        Duration expiry = Duration.ofSeconds(presignProperties.getGetExpirySeconds());
        var presigned = presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(expiry)
                        .getObjectRequest(get)
                        .build()
        );
        Instant expiresAt = Instant.now().plusSeconds(presignProperties.getGetExpirySeconds());
        Map<String, String> headers = toSingleValueHeaders(presigned.signedHeaders());

        return PresignUrlResponse.builder()
                .url(presigned.url().toString())
                .method("GET")
                .expiresAt(expiresAt)
                .bucket(file.getBucket())
                .objectKey(file.getObjectKey())
                .headers(headers)
                .build();
    }

    private String buildObjectKey(UploadPurpose purpose, PresignPutRequest req) {
        String ext = StringUtils.getFilenameExtension(req.getFilename());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String safeExt = StringUtils.hasText(ext) ? "." + ext : "";
        StringBuilder path = new StringBuilder();
        switch (purpose) {
            case THUMBNAIL -> path.append("images/");
            case INTRO_VIDEO, LESSON_VIDEO -> path.append("videos/");
            case DOCUMENT -> path.append("other/");
            default -> path.append("other/");
        }
        if (req.getCourseId() != null) {
            path.append("course-").append(req.getCourseId()).append("/");
        }
        if (req.getLessonId() != null) {
            path.append("lesson-").append(req.getLessonId()).append("/");
        }
        path.append(uuid).append(safeExt);
        return path.toString();
    }

    private S3Presigner presigner() {
        var builder = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Props.getAccessKey(), s3Props.getSecretKey())))
                .region(Region.of(s3Props.getRegion()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(s3Props.isPathStyleAccess())
                        .build());
        if (StringUtils.hasText(s3Props.getEndpoint())) {
            builder = builder.endpointOverride(URI.create(s3Props.getEndpoint()));
        }
        return builder.build();
    }

    private Map<String, String> toSingleValueHeaders(Map<String, java.util.List<String>> multi) {
        Map<String, String> result = new HashMap<>();
        multi.forEach((k, v) -> result.put(k, v.isEmpty() ? "" : v.get(0)));
        return result;
    }
}
