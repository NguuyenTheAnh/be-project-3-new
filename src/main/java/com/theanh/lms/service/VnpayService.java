package com.theanh.lms.service;

import com.theanh.lms.dto.response.PaymentReturnResponse;

import java.util.Map;

public interface VnpayService {

    PaymentReturnResponse handleReturn(Map<String, String> params);

    String handleIpn(Map<String, String> params);
}
