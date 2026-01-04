package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.OrderItemDto;
import com.theanh.lms.entity.OrderItem;
import com.theanh.lms.repository.OrderItemRepository;
import com.theanh.lms.service.OrderItemService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl extends BaseServiceImpl<OrderItem, OrderItemDto, Long> implements OrderItemService {

    private final OrderItemRepository repository;

    public OrderItemServiceImpl(OrderItemRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public List<OrderItemDto> findByOrder(Long orderId) {
        return repository.findByOrder(orderId).stream()
                .map(oi -> modelMapper.map(oi, OrderItemDto.class))
                .toList();
    }

    @Override
    protected Class<OrderItem> getEntityClass() {
        return OrderItem.class;
    }

    @Override
    protected Class<OrderItemDto> getDtoClass() {
        return OrderItemDto.class;
    }
}
