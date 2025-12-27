package com.theanh.lms.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoredFile {
    private String storageProvider;
    private String bucket;
    private String objectKey;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private String checksumSha256;
    private Boolean isPublic;
}
