package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewModerationRequest {
    @NotNull
    private String status; // APPROVED / REJECTED
}
