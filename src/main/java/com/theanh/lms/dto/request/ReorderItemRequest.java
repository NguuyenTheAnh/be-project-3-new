package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReorderItemRequest {
    @NotNull
    private Long id;
    @NotNull
    private Integer position;
}
