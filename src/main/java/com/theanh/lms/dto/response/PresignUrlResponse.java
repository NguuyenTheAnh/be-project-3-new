package com.theanh.lms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class PresignUrlResponse {
    private String url;
    private String method;
    private Instant expiresAt;
    private String bucket;
    private String objectKey;
    private Map<String, String> headers;
}
