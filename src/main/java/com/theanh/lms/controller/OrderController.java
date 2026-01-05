package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.OrderDto;
import com.theanh.lms.dto.request.OrderCreateRequest;
import com.theanh.lms.dto.response.PaymentUrlResponse;
import com.theanh.lms.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<OrderDto>> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        Long userId = currentUserId();
        return ResponseConfig.success(orderService.createOrder(userId, request.getCourseId(), null));
    }

    @PostMapping("/{orderId}/pay/vnpay")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<PaymentUrlResponse>> payVnpay(@PathVariable Long orderId) {
        Long userId = currentUserId();
        return ResponseConfig.success(orderService.createVnpayPaymentUrl(userId, orderId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<Page<OrderDto>>> myOrders(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseConfig.success(orderService.listMyOrders(userId, pageable));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<OrderDto>> myOrderDetail(@PathVariable Long orderId) {
        Long userId = currentUserId();
        return ResponseConfig.success(orderService.getMyOrder(userId, orderId));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<Page<OrderDto>>> adminList(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseConfig.success(orderService.adminListOrders(pageable));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getPrincipal().toString());
    }
}
