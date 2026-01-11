package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.CartDto;
import com.theanh.lms.dto.CartItemDto;
import com.theanh.lms.dto.CartItemResponse;
import com.theanh.lms.dto.CartResponse;
import com.theanh.lms.dto.CourseDto;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.entity.Cart;
import com.theanh.lms.enums.CartStatus;
import com.theanh.lms.repository.CartRepository;
import com.theanh.lms.service.CartItemService;
import com.theanh.lms.service.CartService;
import com.theanh.lms.service.CourseService;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.UploadedFileService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CartServiceImpl extends BaseServiceImpl<Cart, CartDto, Long> implements CartService {

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UploadedFileService uploadedFileService;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemService cartItemService,
                           CourseService courseService,
                           EnrollmentService enrollmentService,
                           UploadedFileService uploadedFileService,
                           ModelMapper modelMapper) {
        super(cartRepository, modelMapper);
        this.cartRepository = cartRepository;
        this.cartItemService = cartItemService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.uploadedFileService = uploadedFileService;
    }

    @Override
    @Transactional
    public CartDto getOrCreateActiveCart(Long userId) {
        return cartRepository.findActiveByUser(userId)
                .map(c -> modelMapper.map(c, CartDto.class))
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .userId(userId)
                            .status(CartStatus.ACTIVE.name())
                            .build();
                    cart.setIsActive(Boolean.TRUE);
                    cart.setIsDeleted(Boolean.FALSE);
                    Cart saved = cartRepository.save(cart);
                    return modelMapper.map(saved, CartDto.class);
                });
    }

    @Override
    public CartDto findActiveById(Long cartId) {
        return cartRepository.findActiveById(cartId)
                .map(c -> modelMapper.map(c, CartDto.class))
                .orElse(null);
    }

    @Override
    @Transactional
    public CartResponse getMyCart(Long userId) {
        CartDto cart = getOrCreateActiveCart(userId);
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, Long courseId) {
        if (enrollmentService.isEnrolled(userId, courseId)) {
            throw new BusinessException("data.fail");
        }
        CourseDto course = courseService.findActivePublishedById(courseId);
        if (course == null) {
            throw new BusinessException("data.not_found");
        }
        CartDto cart = getOrCreateActiveCart(userId);
        CartItemDto existing = cartItemService.findByCartAndCourse(cart.getId(), courseId);
        if (existing == null) {
            CartItemDto item = new CartItemDto();
            item.setCartId(cart.getId());
            item.setCourseId(courseId);
            Long price = course.getPriceCents() != null ? course.getPriceCents() : 0L;
            item.setPriceCents(price);
            item.setFinalPriceCents(price);
            cartItemService.saveObject(item);
        }
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long courseId) {
        CartDto cart = getOrCreateActiveCart(userId);
        CartItemDto existing = cartItemService.findByCartAndCourse(cart.getId(), courseId);
        if (existing != null) {
            cartItemService.deleteById(existing.getId());
        }
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Long userId) {
        CartDto cart = getOrCreateActiveCart(userId);
        cartItemService.deleteByCartId(cart.getId());
        return buildCartResponse(cart);
    }

    private CartResponse buildCartResponse(CartDto cart) {
        List<CartItemDto> items = cartItemService.findByCartId(cart.getId());
        Map<Long, CourseDto> courseMap = loadCourses(items);
        Map<Long, UploadedFileDto> thumbnailMap = loadThumbnails(courseMap);
        long total = 0L;
        List<CartItemResponse> responses = items.stream().map(item -> {
            CartItemResponse resp = new CartItemResponse();
            resp.setCourseId(item.getCourseId());
            CourseDto course = courseMap.get(item.getCourseId());
            if (course != null) {
                resp.setTitle(course.getTitle());
                resp.setPriceCents(course.getPriceCents());
                resp.setFinalPriceCents(item.getFinalPriceCents());
                if (course.getThumbnailFileId() != null) {
                    resp.setThumbnail(thumbnailMap.get(course.getThumbnailFileId()));
                }
            }
            return resp;
        }).toList();
        for (CartItemDto item : items) {
            if (item.getFinalPriceCents() != null) {
                total += item.getFinalPriceCents();
            }
        }
        CartResponse resp = new CartResponse();
        resp.setId(cart.getId());
        resp.setUserId(cart.getUserId());
        resp.setStatus(cart.getStatus());
        resp.setTotalAmountCents(total);
        resp.setItems(responses);
        return resp;
    }

    private Map<Long, CourseDto> loadCourses(List<CartItemDto> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Map.of();
        }
        Map<Long, CourseDto> map = new HashMap<>();
        for (CartItemDto item : items) {
            CourseDto course = courseService.findActiveById(item.getCourseId());
            if (course != null) {
                map.put(item.getCourseId(), course);
            }
        }
        return map;
    }

    private Map<Long, UploadedFileDto> loadThumbnails(Map<Long, CourseDto> courses) {
        if (courses.isEmpty()) {
            return Map.of();
        }
        Map<Long, UploadedFileDto> result = new HashMap<>();
        courses.values().stream()
                .map(CourseDto::getThumbnailFileId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(fid -> {
                    try {
                        result.put(fid, uploadedFileService.findById(fid));
                    } catch (Exception ignored) {
                    }
                });
        return result;
    }

    @Override
    protected Class<Cart> getEntityClass() {
        return Cart.class;
    }

    @Override
    protected Class<CartDto> getDtoClass() {
        return CartDto.class;
    }
}
