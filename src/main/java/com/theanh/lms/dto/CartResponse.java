package com.theanh.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {
    private Long id;
    private Long userId;
    private String status;
    private Long totalAmountCents;
    private List<CartItemResponse> items;
}
