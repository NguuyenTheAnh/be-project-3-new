package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.OrderDto;
import com.theanh.lms.dto.response.PaymentUrlResponse;
import com.theanh.lms.entity.Order;

public interface OrderService extends BaseService<Order, OrderDto, Long> {

    OrderDto createOrder(Long userId, Long courseId, Long priceCents);

    PaymentUrlResponse createVnpayPaymentUrl(Long userId, Long orderId);

    org.springframework.data.domain.Page<OrderDto> listMyOrders(Long userId, org.springframework.data.domain.Pageable pageable);

    OrderDto getMyOrder(Long userId, Long orderId);

    org.springframework.data.domain.Page<OrderDto> adminListOrders(org.springframework.data.domain.Pageable pageable);
}
