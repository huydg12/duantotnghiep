package com.poly.BE_main.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.poly.BE_main.model.PaymentTransaction;
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction,Integer> {
    Optional<PaymentTransaction> findByProviderOrderId(String providerOrderId);
    Optional<PaymentTransaction> findByProviderTransactionId(String providerTransactionId);
}
