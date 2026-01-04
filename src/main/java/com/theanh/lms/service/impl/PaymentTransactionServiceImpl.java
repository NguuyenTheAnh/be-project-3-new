package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.lms.dto.PaymentTransactionDto;
import com.theanh.lms.entity.PaymentTransaction;
import com.theanh.lms.repository.PaymentTransactionRepository;
import com.theanh.lms.service.PaymentTransactionService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentTransactionServiceImpl extends BaseServiceImpl<PaymentTransaction, PaymentTransactionDto, Long> implements PaymentTransactionService {

    private final PaymentTransactionRepository repository;

    public PaymentTransactionServiceImpl(PaymentTransactionRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    @Override
    public PaymentTransactionDto findActiveById(Long id) {
        return repository.findActiveById(id)
                .map(pt -> modelMapper.map(pt, PaymentTransactionDto.class))
                .orElse(null);
    }

    @Override
    public PaymentTransactionDto findByProviderTxn(String provider, String providerTxnId) {
        return repository.findByProviderTxn(provider, providerTxnId)
                .map(pt -> modelMapper.map(pt, PaymentTransactionDto.class))
                .orElse(null);
    }

    @Override
    protected Class<PaymentTransaction> getEntityClass() {
        return PaymentTransaction.class;
    }

    @Override
    protected Class<PaymentTransactionDto> getDtoClass() {
        return PaymentTransactionDto.class;
    }
}
