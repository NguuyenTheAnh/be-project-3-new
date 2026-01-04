package com.theanh.lms.dto.request;

import lombok.Data;

@Data
public class ProgressUpdateRequest {
    private Integer lastPositionSeconds;
    private Boolean completed;
}
