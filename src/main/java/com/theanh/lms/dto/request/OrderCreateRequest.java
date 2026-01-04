package com.theanh.lms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotNull
    private Long courseId;
    @NotNull
    @Min(0)
    private Long priceCents;
}
