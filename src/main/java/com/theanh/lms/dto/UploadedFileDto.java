package com.theanh.lms.dto;

import com.theanh.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UploadedFileDto extends BaseDto {
    private String storageProvider;
    private String bucket;
    private String objectKey;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private String checksumSha256;
    private Boolean isPublic;
    private String accessUrl;
}
