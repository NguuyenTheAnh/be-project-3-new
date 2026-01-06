package com.theanh.lms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payment.vnpay")
public class VnpayProperties {
    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
    private String ipnUrl;
    private String currency;
    private String locale;
    /**
     * Client IP to send to VNPay when real client IP is unavailable (must not be localhost).
     */
    private String clientIp = "8.8.8.8";
    /**
     * Payment link TTL in minutes.
     */
    private Long expireMinutes = 15L;
}
