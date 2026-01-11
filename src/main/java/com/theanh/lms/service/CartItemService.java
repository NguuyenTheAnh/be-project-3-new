package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.CartItemDto;
import com.theanh.lms.entity.CartItem;

import java.util.List;

public interface CartItemService extends BaseService<CartItem, CartItemDto, Long> {

    List<CartItemDto> findByCartId(Long cartId);

    CartItemDto findByCartAndCourse(Long cartId, Long courseId);

    void deleteByCartId(Long cartId);
}
