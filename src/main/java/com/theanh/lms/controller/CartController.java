package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.CartResponse;
import com.theanh.lms.dto.request.CartItemRequest;
import com.theanh.lms.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<CartResponse>> getMyCart() {
        Long userId = currentUserId();
        return ResponseConfig.success(cartService.getMyCart(userId));
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<CartResponse>> addItem(@Valid @RequestBody CartItemRequest request) {
        Long userId = currentUserId();
        return ResponseConfig.success(cartService.addItem(userId, request.getCourseId()));
    }

    @DeleteMapping("/items/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<CartResponse>> removeItem(@PathVariable Long courseId) {
        Long userId = currentUserId();
        return ResponseConfig.success(cartService.removeItem(userId, courseId));
    }

    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<CartResponse>> clearCart() {
        Long userId = currentUserId();
        return ResponseConfig.success(cartService.clearCart(userId));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getPrincipal().toString());
    }
}
