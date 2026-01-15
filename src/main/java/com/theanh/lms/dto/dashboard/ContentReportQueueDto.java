package com.theanh.lms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReportQueueDto {
    private Long reportId;
    private String contentType;
    private Long contentId;
    private Long reporterUserId;
    private String reporterFullName;
    private String reason;
    private String status;
    private LocalDateTime reportedAt;
}
