package com.theanh.lms.dto;

import com.theanh.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentTransactionDto extends BaseDto {
    private Long orderId;
    private String provider;
    private String providerTxnId;
    private Long amountCents;
    private String currency;
    private String status;
    private String rawResponseJson;
    private String txnRef;
    private String paymentUrl;
    private java.time.LocalDateTime expiredAt;
}
