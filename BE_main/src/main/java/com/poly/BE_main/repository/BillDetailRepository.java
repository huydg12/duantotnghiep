package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.poly.BE_main.model.BillDetail;

public interface BillDetailRepository extends JpaRepository<BillDetail, Integer>{
    List<BillDetail> findByBillId(Integer billId);
    @Query(value = """
    SELECT TOP 5 bd.PRODUCT_NAME AS productName,
                 SUM(bd.QUANTITY) AS totalSold
    FROM BILL_DETAIL bd
    JOIN BILL b ON bd.BILL_ID = b.ID

    WHERE b.STATUS = 4
    GROUP BY bd.PRODUCT_NAME
    ORDER BY totalSold DESC
    """, nativeQuery = true)
    List<Object[]> findTop5BestSellingProductsPaid();
}