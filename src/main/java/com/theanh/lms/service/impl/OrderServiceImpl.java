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
import com.theanh.lms.enums.OrderStatus;
import com.theanh.lms.enums.PaymentProvider;
import com.theanh.lms.enums.PaymentStatus;
import com.theanh.lms.repository.OrderItemRepository;
import com.theanh.lms.repository.OrderRepository;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.OrderItemService;
import com.theanh.lms.service.OrderService;
import com.theanh.lms.service.PaymentTransactionService;
import com.theanh.lms.utils.VnpayUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderServiceImpl extends BaseServiceImpl<Order, OrderDto, Long> implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionService paymentTransactionService;
    private final EnrollmentService enrollmentService;
    private final VnpayProperties vnpayProperties;

    public OrderServiceImpl(OrderRepository repository,
                            OrderItemService orderItemService,
                            OrderItemRepository orderItemRepository,
                            PaymentTransactionService paymentTransactionService,
                            EnrollmentService enrollmentService,
                            VnpayProperties vnpayProperties,
                            ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.orderRepository = repository;
        this.orderItemService = orderItemService;
        this.orderItemRepository = orderItemRepository;
        this.paymentTransactionService = paymentTransactionService;
        this.enrollmentService = enrollmentService;
        this.vnpayProperties = vnpayProperties;
    }

    @Override
    @Transactional
    public OrderDto createOrder(Long userId, Long courseId, Long priceCents) {
        if (enrollmentService.isEnrolled(userId, courseId)) {
            throw new BusinessException("data.fail");
        }
        Optional<Order> existingPending = orderRepository.findLatestByUserAndStatus(userId, OrderStatus.PENDING.name());
        if (existingPending.isPresent()) {
            List<OrderItemDto> items = orderItemRepository.findByOrder(existingPending.get().getId()).stream()
                    .filter(oi -> !Boolean.TRUE.equals(oi.getIsDeleted()))
                    .map(oi -> modelMapper.map(oi, OrderItemDto.class))
                    .toList();
            boolean hasCourse = items.stream().anyMatch(i -> courseId.equals(i.getCourseId()));
            if (hasCourse) {
                return modelMapper.map(existingPending.get(), OrderDto.class);
            }
        }
        OrderDto order = new OrderDto();
        order.setUserId(userId);
        order.setTotalAmountCents(priceCents);
        order.setCurrency(vnpayProperties.getCurrency());
        order.setStatus(OrderStatus.PENDING.name());
        order.setPaymentMethod(PaymentProvider.VNPAY.name());
        OrderDto savedOrder = saveObject(order);

        OrderItemDto item = new OrderItemDto();
        item.setOrderId(savedOrder.getId());
        item.setCourseId(courseId);
        item.setPriceCents(priceCents);
        item.setDiscountCents(0L);
        item.setFinalPriceCents(priceCents);
        orderItemService.saveObject(item);
        return savedOrder;
    }

    @Override
    public PaymentUrlResponse createVnpayPaymentUrl(Long userId, Long orderId) {
        Order order = orderRepository.findActiveById(orderId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("data.fail");
        }
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new BusinessException("data.fail");
        }
        long amount = order.getTotalAmountCents() * 100; // VNPAY amount in VND * 100
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", vnpayProperties.getCurrency());
        params.put("vnp_TxnRef", String.valueOf(order.getId()));
        params.put("vnp_OrderInfo", "Order " + order.getId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", vnpayProperties.getLocale());
        params.put("vnp_ReturnUrl", vnpayProperties.getReturnUrl());
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        params.put("vnp_IpnUrl", vnpayProperties.getIpnUrl());
        String payUrl = VnpayUtils.buildSignedUrl(vnpayProperties.getPayUrl(), params, vnpayProperties.getHashSecret());

        PaymentTransactionDto txn = new PaymentTransactionDto();
        txn.setOrderId(order.getId());
        txn.setProvider(PaymentProvider.VNPAY.name());
        txn.setAmountCents(order.getTotalAmountCents());
        txn.setCurrency(order.getCurrency());
        txn.setStatus(PaymentStatus.INIT.name());
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
    protected Class<Order> getEntityClass() {
        return Order.class;
    }

    @Override
    protected Class<OrderDto> getDtoClass() {
        return OrderDto.class;
    }
}
