package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModerationActionRequest {
    @NotBlank
    private String action; // APPROVE/REJECT/HIDE/BLOCK_USER
    private String notes;
}
