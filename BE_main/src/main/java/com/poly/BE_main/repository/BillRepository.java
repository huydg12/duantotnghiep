package com.poly.BE_main.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poly.BE_main.dto.InvoiceItemCustomerDTO;
import com.poly.BE_main.dto.RevenueByBrandDTO;
import com.poly.BE_main.dto.StockStatisticDTO;
import com.poly.BE_main.dto.TopSellingProductDTO;
import com.poly.BE_main.model.Bill;

public interface BillRepository extends JpaRepository<Bill, Integer> {
    @Query("""
                SELECT SUM(b.grandTotal) FROM Bill b WHERE b.statusPayment = 'DA_THANH_TOAN'
            """)
    BigDecimal getTotalRevenue();

    @Query("""
                SELECT COUNT(b) FROM Bill b WHERE b.statusPayment = 'DA_THANH_TOAN'
            """)
    Long getPaidBillCount();

    @Query("""
                SELECT SUM(bd.quantity)
                FROM BillDetail bd
                JOIN bd.bill b
                WHERE b.statusPayment = 'DA_THANH_TOAN'
            """)
    Long getTotalProductSold();

    @Query("""
                SELECT COUNT(b)
                FROM Bill b
                WHERE b.statusPayment = 'DA_THANH_TOAN' AND b.promotionId IS NOT NULL
            """)
    Long getPromoBillCount();

    @Query("""
                SELECT SUM(b.discountAmount)
                FROM Bill b
                WHERE b.statusPayment = 'DA_THANH_TOAN'
            """)
    BigDecimal getTotalDiscountAmount();

    @Query("""
                SELECT new com.poly.BE_main.dto.TopSellingProductDTO(
                    p.productName,
                    SUM(bd.quantity),
                    SUM(bd.price * bd.quantity)
                )
                FROM BillDetail bd
                JOIN bd.productDetail pd
                JOIN pd.product p
                JOIN bd.bill b
                WHERE b.statusPayment = 'DA_THANH_TOAN'
                GROUP BY p.productName
                ORDER BY SUM(bd.quantity) DESC
            """)
    List<TopSellingProductDTO> getTopSellingProducts();

    @Query("""
                SELECT new com.poly.BE_main.dto.TopSellingProductDTO(p.productName, SUM(bd.quantity), SUM(bd.price * bd.quantity))
                FROM BillDetail bd
                JOIN bd.productDetail pd
                JOIN pd.product p
                JOIN bd.bill b
                WHERE b.statusPayment = 'DA_THANH_TOAN'
                AND b.createdDate BETWEEN :from AND :to
                GROUP BY p.productName
                ORDER BY SUM(bd.quantity) DESC
            """)
    List<TopSellingProductDTO> getTopSellingProductsBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
                SELECT new com.poly.BE_main.dto.RevenueByBrandDTO(br.name, SUM(bd.price * bd.quantity))
                FROM BillDetail bd
                JOIN bd.productDetail pd
                JOIN pd.product p
                JOIN p.brand br
                JOIN bd.bill b
                WHERE b.statusPayment = 'DA_THANH_TOAN'
                GROUP BY br.name
                ORDER BY SUM(bd.price * bd.quantity) DESC
            """)
    List<RevenueByBrandDTO> getRevenueByBrand();

    @Query("""
                SELECT new com.poly.BE_main.dto.StockStatisticDTO(
                    p.productName,
                    c.name,
                    s.eu,
                    SUM(bd.quantity)
                )
                FROM BillDetail bd
                JOIN bd.productDetail pd
                JOIN pd.product p
                JOIN pd.color c
                JOIN pd.size s
                GROUP BY p.productName, c.name, s.eu
            """)
    List<StockStatisticDTO> getStockStatistics();

    @Query("""
                SELECT new com.poly.BE_main.dto.RevenueByBrandDTO(
                    p.brand.name,
                    SUM(b.grandTotal)
                )
                FROM Bill b
                JOIN b.billDetails bd
                JOIN bd.productDetail pd
                JOIN pd.product p
                WHERE b.status = 1 AND b.dateOfPayment BETWEEN :from AND :to
                GROUP BY p.brand.name
            """)
    List<RevenueByBrandDTO> getRevenueByBrandFiltered(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
                SELECT new com.poly.BE_main.dto.StockStatisticDTO(
                    p.productName,
                    c.name,
                    s.eu,
                    SUM(bd.quantity)
                )
                FROM BillDetail bd
                JOIN bd.productDetail pd
                JOIN pd.product p
                JOIN pd.color c
                JOIN pd.size s
                JOIN bd.bill b
                WHERE b.status = 1
                  AND b.dateOfPayment BETWEEN :from AND :to
                GROUP BY p.productName, c.name, s.eu
            """)
    List<StockStatisticDTO> getStockStatisticsFiltered(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    List<Bill> findByCustomerId(Integer customerId);

    @Query("""
                SELECT new com.poly.BE_main.dto.InvoiceItemCustomerDTO(
                    p.productName,
                    c.name,
                    CAST(s.eu AS string),
                    bd.quantity,
                    bd.price,
                    COALESCE((SELECT img.url
                             FROM Image img
                             WHERE img.productDetailId = pd.id
                               AND img.isMain = true), '')
                )
                FROM BillDetail bd
                JOIN bd.productDetail pd
                JOIN pd.product p
                JOIN pd.color c
                JOIN pd.size s
                JOIN bd.bill b
                WHERE b.customerId = :customerId
                  AND b.id = :billId
            """)
    List<InvoiceItemCustomerDTO> findInvoiceItemsByCustomerIdAndBillId(
            @Param("customerId") Integer customerId,
            @Param("billId") Integer billId);
}