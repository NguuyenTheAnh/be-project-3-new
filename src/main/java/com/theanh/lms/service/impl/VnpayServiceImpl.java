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
import com.theanh.lms.service.PaymentTransactionService;
import com.theanh.lms.service.OrderItemService;
import com.theanh.lms.service.VnpayService;
import com.theanh.lms.utils.VnpayUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnpayServiceImpl implements VnpayService {

    private final VnpayProperties vnpayProperties;
    private final OrderRepository orderRepository;
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
            return ipnResponse("97", "Invalid signature");
        }
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        Long orderId = parseLong(txnRef);
        if (orderId == null) {
            return ipnResponse("99", "Invalid order");
        }
        log.info("VNPay IPN received for order {} with responseCode {}", orderId, responseCode);
        Optional<Order> orderOpt = orderRepository.findActiveById(orderId);
        if (orderOpt.isEmpty()) {
            return ipnResponse("01", "Order not found");
        }
        Order order = orderOpt.get();
        if (OrderStatus.PAID.name().equals(order.getStatus())) {
            return ipnResponse("00", "Order already paid");
        }
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            return ipnResponse("02", "Order status invalid");
        }
        // amount check: vnp_Amount is amount * 100
        Long vnpAmount = parseLong(params.get("vnp_Amount"));
        long expectedAmount = order.getTotalAmountCents() * 100;
        if (vnpAmount == null || vnpAmount.longValue() != expectedAmount) {
            log.warn("VNPay amount mismatch for order {} expected {} got {}", orderId, expectedAmount, vnpAmount);
            order.setStatus(OrderStatus.FAILED.name());
            orderRepository.save(order);
            PaymentTransactionDto txn = new PaymentTransactionDto();
            txn.setOrderId(orderId);
            txn.setProvider(PaymentProvider.VNPAY.name());
            txn.setProviderTxnId(params.get("vnp_TransactionNo"));
            txn.setAmountCents(order.getTotalAmountCents());
            txn.setCurrency(order.getCurrency());
            txn.setStatus(PaymentStatus.FAILED.name());
            txn.setRawResponseJson(params.toString());
            paymentTransactionService.saveObject(txn);
            return ipnResponse("04", "Invalid amount");
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
        } else if (PaymentStatus.INIT.name().equals(existingTxn.getStatus())) {
            existingTxn.setStatus(success ? PaymentStatus.SUCCESS.name() : PaymentStatus.FAILED.name());
            existingTxn.setRawResponseJson(params.toString());
            paymentTransactionService.saveObject(existingTxn);
        } else {
            log.info("VNPay IPN already processed for providerTxn {}", params.get("vnp_TransactionNo"));
        }
        if (!success) {
            order.setStatus(OrderStatus.FAILED.name());
            orderRepository.save(order);
            log.warn("VNPay IPN failed for order {}", orderId);
            return ipnResponse("99", "Payment failed");
        }
        order.setStatus(OrderStatus.PAID.name());
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("VNPay IPN success for order {}", orderId);
        // Auto-enroll each course in the order
        orderItemService.findByOrder(orderId).forEach(item -> {
            try {
                if (!enrollmentService.isEnrolled(order.getUserId(), item.getCourseId())) {
                    enrollmentService.enroll(order.getUserId(), item.getCourseId());
                }
            } catch (Exception ignored) {
            }
        });
        return ipnResponse("00", "Success");
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String ipnResponse(String code, String message) {
        return "RspCode=" + code + "&Message=" + message;
    }
}
