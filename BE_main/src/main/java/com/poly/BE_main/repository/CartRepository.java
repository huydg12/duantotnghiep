package com.poly.BE_main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poly.BE_main.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    @Query(value = """
            SELECT
                ID                 AS cartId
            FROM CART
            WHERE CUSTOMER_ID = :customerId
                        """, nativeQuery = true)
    Integer findCartIdByCustomerId(@Param("customerId") Integer customerId);
}