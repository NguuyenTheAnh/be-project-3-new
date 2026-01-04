package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends BaseRepository<PaymentTransaction, Long> {

    @Query(value = """
            SELECT * FROM payment_transaction pt
            WHERE pt.id = :id
              AND (pt.is_deleted IS NULL OR pt.is_deleted = 0)
            """, nativeQuery = true)
    Optional<PaymentTransaction> findActiveById(@Param("id") Long id);

    @Query(value = """
            SELECT * FROM payment_transaction pt
            WHERE pt.provider = :provider
              AND pt.provider_txn_id = :providerTxnId
              AND (pt.is_deleted IS NULL OR pt.is_deleted = 0)
            """, nativeQuery = true)
    Optional<PaymentTransaction> findByProviderTxn(@Param("provider") String provider,
                                                   @Param("providerTxnId") String providerTxnId);
}
