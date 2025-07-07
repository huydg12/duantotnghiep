package com.poly.BE_main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poly.BE_main.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
Optional<Customer> findByAccountId(int accountId);
    @Query(value = """
            SELECT
                FULL_NAME                 AS fullName,
                NUMBER_PHONE               AS numberPhone
            FROM CUSTOMER
            WHERE ID = :customerId
            """, nativeQuery = true)
    List<Object[]> findCustomerByCustomerId(@Param("customerId") Integer customerId);
}