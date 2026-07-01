package com.poly.BE_main.repository;

import com.poly.BE_main.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    Optional<PaymentTransaction> findByProviderOrderId(String providerOrderId);

    Optional<PaymentTransaction> findByProviderTransactionId(String providerTransactionId);
}
