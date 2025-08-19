package com.poly.BE_main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query(value = """
                SELECT
                    c.FULL_NAME AS fullName,
                    c.GENDER as gender,
                    c.EMAIL AS email,
                    c.NUMBER_PHONE AS numberPhone,
                    c.BIRTH_OF_DATE AS birthOfDate
                FROM CUSTOMER c
                JOIN ACCOUNT a ON c.ACCOUNT_ID = a.ID
                WHERE c.ID = :customerId
            """, nativeQuery = true)
    Object findInformationCustomerByCustomerId(@Param("customerId") Integer customerId);

    @Modifying
    @Query(value = """
                UPDATE CUSTOMER
                SET
                    FULL_NAME = :fullName,
                    GENDER = :gender,
                    EMAIL = :email,
                    NUMBER_PHONE = :numberPhone,
                    BIRTH_OF_DATE = :birthOfDate
                WHERE ID = :customerId
            """, nativeQuery = true)
    int updateInformationCustomerByCustomerId(
            @Param("customerId") Integer customerId,
            @Param("fullName") String fullName,
            @Param("gender") String gender,
            @Param("email") String email,
            @Param("numberPhone") String numberPhone,
            @Param("birthOfDate") java.sql.Date birthOfDate);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}