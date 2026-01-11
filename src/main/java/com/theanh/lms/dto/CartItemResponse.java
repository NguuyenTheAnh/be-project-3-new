package com.theanh.lms.dto;

import lombok.Data;

@Data
public class CartItemResponse {
    private Long courseId;
    private String title;
    private Long priceCents;
    private Long finalPriceCents;
    private UploadedFileDto thumbnail;
}
