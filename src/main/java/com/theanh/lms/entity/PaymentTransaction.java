package com.theanh.lms.entity;

import com.theanh.lms.common.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentTransaction extends BaseAuditEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "provider", length = 50, nullable = false)
    private String provider;

    @Column(name = "provider_txn_id", length = 255)
    private String providerTxnId;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "raw_response_json", columnDefinition = "longtext")
    private String rawResponseJson;

    @Column(name = "txn_ref", length = 255)
    private String txnRef;

    @Column(name = "payment_url", columnDefinition = "text")
    private String paymentUrl;

    @Column(name = "expired_at")
    private java.time.LocalDateTime expiredAt;
}
