package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.CartDto;
import com.theanh.lms.dto.CartResponse;
import com.theanh.lms.entity.Cart;

public interface CartService extends BaseService<Cart, CartDto, Long> {

    CartDto getOrCreateActiveCart(Long userId);

    CartDto findActiveById(Long cartId);

    CartResponse getMyCart(Long userId);

    CartResponse addItem(Long userId, Long courseId);

    CartResponse removeItem(Long userId, Long courseId);

    CartResponse clearCart(Long userId);
}
