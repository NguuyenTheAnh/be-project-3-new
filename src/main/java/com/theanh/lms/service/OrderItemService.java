package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.OrderItemDto;
import com.theanh.lms.entity.OrderItem;

import java.util.List;

public interface OrderItemService extends BaseService<OrderItem, OrderItemDto, Long> {

    List<OrderItemDto> findByOrder(Long orderId);
}
