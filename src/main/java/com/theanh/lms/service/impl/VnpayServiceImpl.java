package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.config.VnpayProperties;
import com.theanh.lms.dto.OrderDto;
import com.theanh.lms.dto.PaymentTransactionDto;
import com.theanh.lms.dto.response.PaymentReturnResponse;
import com.theanh.lms.entity.Order;
import com.theanh.lms.enums.OrderStatus;
import com.theanh.lms.enums.PaymentProvider;
import com.theanh.lms.enums.PaymentStatus;
import com.theanh.lms.repository.OrderRepository;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.OrderService;
import com.theanh.lms.service.PaymentTransactionService;
import com.theanh.lms.service.OrderItemService;
import com.theanh.lms.service.VnpayService;
import com.theanh.lms.utils.VnpayUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VnpayServiceImpl implements VnpayService {

    private final VnpayProperties vnpayProperties;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentTransactionService paymentTransactionService;
    private final EnrollmentService enrollmentService;
    private final OrderItemService orderItemService;

    @Override
    public PaymentReturnResponse handleReturn(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (!VnpayUtils.verifySignature(params, secureHash, vnpayProperties.getHashSecret())) {
            return PaymentReturnResponse.builder().status("FAILED").message("Invalid signature").build();
        }
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            return PaymentReturnResponse.builder().status("SUCCESS").message("Payment success").build();
        }
        return PaymentReturnResponse.builder().status("FAILED").message("Payment failed").build();
    }

    @Override
    @Transactional
    public String handleIpn(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (!VnpayUtils.verifySignature(params, secureHash, vnpayProperties.getHashSecret())) {
            return "Invalid signature";
        }
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        Long orderId = parseLong(txnRef);
        if (orderId == null) {
            return "Invalid order";
        }
        Optional<Order> orderOpt = orderRepository.findActiveById(orderId);
        if (orderOpt.isEmpty()) {
            return "Order not found";
        }
        Order order = orderOpt.get();
        if (OrderStatus.PAID.name().equals(order.getStatus())) {
            return "OK";
        }
        boolean success = "00".equals(responseCode);
        PaymentTransactionDto existingTxn = paymentTransactionService.findByProviderTxn(
                PaymentProvider.VNPAY.name(), params.get("vnp_TransactionNo"));
        if (existingTxn == null) {
            PaymentTransactionDto txn = new PaymentTransactionDto();
            txn.setOrderId(orderId);
            txn.setProvider(PaymentProvider.VNPAY.name());
            txn.setProviderTxnId(params.get("vnp_TransactionNo"));
            txn.setAmountCents(order.getTotalAmountCents());
            txn.setCurrency(order.getCurrency());
            txn.setStatus(success ? PaymentStatus.SUCCESS.name() : PaymentStatus.FAILED.name());
            txn.setRawResponseJson(params.toString());
            paymentTransactionService.saveObject(txn);
        }
        if (!success) {
            order.setStatus(OrderStatus.FAILED.name());
            orderRepository.save(order);
            return "Failed";
        }
        order.setStatus(OrderStatus.PAID.name());
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        // Auto-enroll each course in the order
        orderItemService.findByOrder(orderId).forEach(item -> {
            try {
                if (!enrollmentService.isEnrolled(order.getUserId(), item.getCourseId())) {
                    enrollmentService.enroll(order.getUserId(), item.getCourseId());
                }
            } catch (Exception ignored) {
            }
        });
        return "OK";
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
}
