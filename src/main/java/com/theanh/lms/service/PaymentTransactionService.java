package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.PaymentTransactionDto;
import com.theanh.lms.entity.PaymentTransaction;

public interface PaymentTransactionService extends BaseService<PaymentTransaction, PaymentTransactionDto, Long> {

    PaymentTransactionDto findActiveById(Long id);

    PaymentTransactionDto findByProviderTxn(String provider, String providerTxnId);
}
