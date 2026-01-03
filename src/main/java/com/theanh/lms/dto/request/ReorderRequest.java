package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReorderRequest {
    @NotEmpty
    private List<ReorderItemRequest> items;
}
