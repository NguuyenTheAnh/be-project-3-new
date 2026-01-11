package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.CartItemDto;
import com.theanh.lms.entity.CartItem;
import com.theanh.lms.repository.CartItemRepository;
import com.theanh.lms.service.CartItemService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemServiceImpl extends BaseServiceImpl<CartItem, CartItemDto, Long> implements CartItemService {

    private final CartItemRepository repository;

    public CartItemServiceImpl(CartItemRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public List<CartItemDto> findByCartId(Long cartId) {
        return repository.findByCartId(cartId).stream()
                .map(item -> modelMapper.map(item, CartItemDto.class))
                .toList();
    }

    @Override
    public CartItemDto findByCartAndCourse(Long cartId, Long courseId) {
        return repository.findByCartAndCourse(cartId, courseId)
                .map(item -> modelMapper.map(item, CartItemDto.class))
                .orElse(null);
    }

    @Override
    public void deleteByCartId(Long cartId) {
        List<Long> ids = repository.findByCartId(cartId)
                .stream()
                .map(CartItem::getId)
                .toList();
        if (!ids.isEmpty()) {
            deleteByIds(ids);
        }
    }

    @Override
    protected Class<CartItem> getEntityClass() {
        return CartItem.class;
    }

    @Override
    protected Class<CartItemDto> getDtoClass() {
        return CartItemDto.class;
    }
}
