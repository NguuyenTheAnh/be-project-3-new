package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.OrderDto;
import com.theanh.lms.dto.PaymentTransactionDto;
import com.theanh.lms.dto.response.PaymentReturnResponse;
import com.theanh.lms.service.VnpayService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/payments/vnpay")
@RequiredArgsConstructor
public class VnpayController {

    private final VnpayService vnpayService;

    @GetMapping("/return")
    public void vnpayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        PaymentReturnResponse result = vnpayService.handleReturn(params);
        // simple redirect; in real FE, change URL accordingly
        String redirectUrl = result.getStatus().equalsIgnoreCase("SUCCESS")
                ? "http://localhost:3000/payment/success"
                : "http://localhost:3000/payment/fail";
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/ipn")
    public ResponseEntity<String> vnpayIpn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(vnpayService.handleIpn(params));
    }
}
