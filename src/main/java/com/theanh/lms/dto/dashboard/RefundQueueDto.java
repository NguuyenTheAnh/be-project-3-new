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
public class RefundQueueDto {
    private Long refundRequestId;
    private Long orderId;
    private Long userId;
    private String userFullName;
    private String reason;
    private Long requestedAmountCents;
    private String status;
    private LocalDateTime requestedAt;
}
