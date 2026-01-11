package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.config.VnpayProperties;
import com.theanh.lms.dto.EnrollmentDto;
import com.theanh.lms.dto.OrderDto;
import com.theanh.lms.dto.OrderItemDto;
import com.theanh.lms.dto.PaymentTransactionDto;
import com.theanh.lms.dto.response.PaymentUrlResponse;
import com.theanh.lms.entity.Order;
import com.theanh.lms.dto.CourseDto;
import com.theanh.lms.dto.CartItemDto;
import com.theanh.lms.enums.OrderStatus;
import com.theanh.lms.enums.PaymentProvider;
import com.theanh.lms.enums.PaymentStatus;
import com.theanh.lms.enums.CartStatus;
import com.theanh.lms.repository.OrderRepository;
import com.theanh.lms.service.CourseService;
import com.theanh.lms.service.CartService;
import com.theanh.lms.service.CartItemService;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.OrderItemService;
import com.theanh.lms.service.OrderService;
import com.theanh.lms.service.PaymentTransactionService;
import com.theanh.lms.utils.VnpayUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

@Service
@Slf4j
public class OrderServiceImpl extends BaseServiceImpl<Order, OrderDto, Long> implements OrderService {

    private static final Duration PENDING_TTL = Duration.ofMinutes(30);

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final CartService cartService;
    private final CartItemService cartItemService;
    private final PaymentTransactionService paymentTransactionService;
    private final EnrollmentService enrollmentService;
    private final VnpayProperties vnpayProperties;
    private final CourseService courseService;

    public OrderServiceImpl(OrderRepository repository,
                            OrderItemService orderItemService,
                            CartService cartService,
                            CartItemService cartItemService,
                            PaymentTransactionService paymentTransactionService,
                            EnrollmentService enrollmentService,
                            VnpayProperties vnpayProperties,
                            CourseService courseService,
                            ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.orderRepository = repository;
        this.orderItemService = orderItemService;
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.paymentTransactionService = paymentTransactionService;
        this.enrollmentService = enrollmentService;
        this.vnpayProperties = vnpayProperties;
        this.courseService = courseService;
    }

    @Override
    @Transactional
    public OrderDto createOrder(Long userId, Long courseId, Long ignoredPrice) {
        cancelExpiredPendingOrders();
        if (enrollmentService.isEnrolled(userId, courseId)) {
            throw new BusinessException("data.fail");
        }
        CourseDto course = courseService.findActivePublishedById(courseId);
        if (course == null) {
            throw new BusinessException("data.not_found");
        }
        long priceCents = course.getPriceCents() != null && course.getPriceCents() >= 0 ? course.getPriceCents() : 0L;
        Optional<Order> existingPending = orderRepository.findLatestByUserAndStatus(userId, OrderStatus.PENDING.name());
        if (existingPending.isPresent()) {
            if (isExpired(existingPending.get())) {
                cancelOrder(existingPending.get(), OrderStatus.CANCELLED.name());
            } else {
                List<OrderItemDto> items = orderItemService.findByOrder(existingPending.get().getId());
                boolean hasCourse = items.stream().anyMatch(i -> courseId.equals(i.getCourseId()));
                if (hasCourse) {
                    return modelMapper.map(existingPending.get(), OrderDto.class);
                }
            }
        }
        OrderDto order = new OrderDto();
        order.setUserId(userId);
        order.setTotalAmountCents(priceCents);
        order.setCurrency(vnpayProperties.getCurrency());
        order.setStatus(OrderStatus.PENDING.name());
        order.setPaymentMethod(PaymentProvider.VNPAY.name());
        OrderDto savedOrder = saveObject(order);
        log.info("Created order {} for user {} course {} amount {}", savedOrder.getId(), userId, courseId, priceCents);

        OrderItemDto item = new OrderItemDto();
        item.setOrderId(savedOrder.getId());
        item.setCourseId(courseId);
        item.setPriceCents(priceCents);
        item.setDiscountCents(0L);
        item.setFinalPriceCents(priceCents);
        orderItemService.saveObject(item);
        if (priceCents == 0L) {
            markPaidAndEnroll(savedOrder.getId(), userId);
            savedOrder = findById(savedOrder.getId());
        }
        return savedOrder;
    }

    @Override
    @Transactional
    public OrderDto createOrderFromCart(Long userId, Long cartId) {
        cancelExpiredPendingOrders();
        var cart = cartId != null ? cartService.findActiveById(cartId) : cartService.getOrCreateActiveCart(userId);
        if (cart == null || !Objects.equals(cart.getUserId(), userId)) {
            throw new BusinessException("data.not_found");
        }
        if (!CartStatus.ACTIVE.name().equals(cart.getStatus())) {
            throw new BusinessException("data.fail");
        }
        List<CartItemDto> items = cartItemService.findByCartId(cart.getId());
        if (CollectionUtils.isEmpty(items)) {
            throw new BusinessException("data.fail");
        }
        long total = 0L;
        List<OrderItemDto> orderItems = new java.util.ArrayList<>();
        for (com.theanh.lms.dto.CartItemDto item : items) {
            if (enrollmentService.isEnrolled(userId, item.getCourseId())) {
                throw new BusinessException("data.fail");
            }
            CourseDto course = courseService.findActivePublishedById(item.getCourseId());
            if (course == null) {
                throw new BusinessException("data.not_found");
            }
            long price = course.getPriceCents() != null ? course.getPriceCents() : 0L;
            total += price;
            OrderItemDto oi = new OrderItemDto();
            oi.setCourseId(course.getId());
            oi.setPriceCents(price);
            oi.setDiscountCents(0L);
            oi.setFinalPriceCents(price);
            orderItems.add(oi);
        }
        OrderDto order = new OrderDto();
        order.setUserId(userId);
        order.setTotalAmountCents(total);
        order.setCurrency(vnpayProperties.getCurrency());
        order.setStatus(OrderStatus.PENDING.name());
        order.setPaymentMethod(PaymentProvider.VNPAY.name());
        OrderDto savedOrder = saveObject(order);
        for (OrderItemDto oi : orderItems) {
            oi.setOrderId(savedOrder.getId());
            orderItemService.saveObject(oi);
        }
        if (total == 0L) {
            markPaidAndEnroll(savedOrder.getId(), userId);
            savedOrder = findById(savedOrder.getId());
        }
        cart.setStatus(CartStatus.CHECKED_OUT.name());
        cartService.saveObject(cart);
        return savedOrder;
    }

    @Override
    public PaymentUrlResponse createVnpayPaymentUrl(Long userId, Long orderId) {
        Order order = orderRepository.findActiveById(orderId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("data.fail");
        }
        if (isExpired(order)) {
            cancelOrder(order, OrderStatus.CANCELLED.name());
            throw new BusinessException("order.expired");
        }
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new BusinessException("data.fail");
        }
        log.info("Generating VNPay URL for order {}", orderId);
        // totalAmountCents is actually stored as VND (not cents) at the moment, VNPay requires VND * 100
        long amount = order.getTotalAmountCents() * 100;
        // VNPay TxnRef must be alphanumeric only (no special chars)
        // TxnRef: numeric only to avoid gateway rejection; unique per order
        String txnRef = String.valueOf(order.getId());
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", vnpayProperties.getCurrency());
        params.put("vnp_TxnRef", txnRef);
        // OrderInfo: no special chars, no accents
        params.put("vnp_OrderInfo", "Order " + order.getId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", vnpayProperties.getLocale());
        params.put("vnp_ReturnUrl", vnpayProperties.getReturnUrl());
        params.put("vnp_IpAddr", vnpayProperties.getClientIp());
        LocalDateTime now = LocalDateTime.now();
        params.put("vnp_CreateDate", now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        if (vnpayProperties.getExpireMinutes() != null) {
            params.put("vnp_ExpireDate", now.plusMinutes(vnpayProperties.getExpireMinutes())
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        }
        String payUrl = VnpayUtils.buildSignedUrl(vnpayProperties.getPayUrl(), params, vnpayProperties.getHashSecret());
        log.info("VNPay hashData={}, secureHash(prefix/suffix)={}...{}, url={}",
                VnpayUtils.buildHashDataPreview(params),
                VnpayUtils.computeSecureHashPreview(vnpayProperties.getHashSecret(), params),
                VnpayUtils.computeSecureHashPreview(vnpayProperties.getHashSecret(), params, true),
                payUrl);

        PaymentTransactionDto txn = new PaymentTransactionDto();
        txn.setOrderId(order.getId());
        txn.setProvider(PaymentProvider.VNPAY.name());
        txn.setTxnRef(txnRef);
        txn.setAmountCents(order.getTotalAmountCents());
        txn.setCurrency(order.getCurrency());
        txn.setStatus(PaymentStatus.INIT.name());
        txn.setPaymentUrl(payUrl);
        if (vnpayProperties.getExpireMinutes() != null) {
            txn.setExpiredAt(LocalDateTime.now().plusMinutes(vnpayProperties.getExpireMinutes()));
        }
        paymentTransactionService.saveObject(txn);

        return PaymentUrlResponse.builder().paymentUrl(payUrl).build();
    }

    @Override
    public org.springframework.data.domain.Page<OrderDto> listMyOrders(Long userId, org.springframework.data.domain.Pageable pageable) {
        return orderRepository.findByUser(userId, pageable)
                .map(o -> modelMapper.map(o, OrderDto.class));
    }

    @Override
    public OrderDto getMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findActiveById(orderId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("data.fail");
        }
        return modelMapper.map(order, OrderDto.class);
    }

    @Override
    public org.springframework.data.domain.Page<OrderDto> adminListOrders(org.springframework.data.domain.Pageable pageable) {
        return orderRepository.findAllActive(pageable)
                .map(o -> modelMapper.map(o, OrderDto.class));
    }

    @Override
    @Transactional
    public void cancelExpiredPendingOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minus(PENDING_TTL);
        List<Order> expired = orderRepository.findPendingBefore(cutoff);
        if (CollectionUtils.isEmpty(expired)) {
            return;
        }
        expired.forEach(o -> cancelOrder(o, OrderStatus.CANCELLED.name()));
        log.info("Cancelled {} expired pending orders", expired.size());
    }

    @Transactional
    public void markPaidAndEnroll(Long orderId, Long userId) {
        Order order = orderRepository.findActiveById(orderId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        order.setStatus(OrderStatus.PAID.name());
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        orderItemService.findByOrder(orderId).forEach(item -> {
            try {
                if (!enrollmentService.isEnrolled(userId, item.getCourseId())) {
                    enrollmentService.enroll(userId, item.getCourseId());
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void cancelOrder(Order order, String status) {
        order.setStatus(status);
        order.setPaidAt(null);
        orderRepository.save(order);
    }

    private boolean isExpired(Order order) {
        return order.getCreatedDate() != null && order.getCreatedDate().isBefore(LocalDateTime.now().minus(PENDING_TTL));
    }

    @Override
    protected Class<Order> getEntityClass() {
        return Order.class;
    }

    @Override
    protected Class<OrderDto> getDtoClass() {
        return OrderDto.class;
    }
}
