package com.poly.BE_main.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poly.BE_main.dto.InvoiceItemCustomerDTO;
import com.poly.BE_main.dto.RevenueByBrandDTO;
import com.poly.BE_main.dto.StockStatisticDTO;
import com.poly.BE_main.dto.TopSellingProductDTO;
import com.poly.BE_main.model.Bill;

public interface BillRepository extends JpaRepository<Bill, Integer> {

  @Query("""
      SELECT SUM(b.grandTotal)
      FROM Bill b
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
      """)
  BigDecimal getTotalRevenue();

  @Query("""
      SELECT COUNT(b)
      FROM Bill b
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
      """)
  Long getPaidBillCount();

  @Query("""
      SELECT SUM(bd.quantity)
      FROM BillDetail bd
      JOIN bd.bill b
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
      """)
  Long getTotalProductSold();

  @Query("""
      SELECT COUNT(b)
      FROM Bill b
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
        AND b.promotionId IS NOT NULL
      """)
  Long getPromoBillCount();

  @Query("""
      SELECT SUM(b.discountAmount)
      FROM Bill b
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
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
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
      GROUP BY p.productName
      ORDER BY SUM(bd.quantity) DESC
      """)
  List<TopSellingProductDTO> getTopSellingProducts();

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
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
        AND b.dateOfPayment BETWEEN :from AND :to
      GROUP BY p.productName
      ORDER BY SUM(bd.quantity) DESC
      """)
  List<TopSellingProductDTO> getTopSellingProductsBetween(@Param("from") LocalDate from,
      @Param("to") LocalDate to);

  @Query("""
      SELECT new com.poly.BE_main.dto.RevenueByBrandDTO(
          br.name,
          SUM(bd.price * bd.quantity)
      )
      FROM BillDetail bd
      JOIN bd.productDetail pd
      JOIN pd.product p
      JOIN p.brand br
      JOIN bd.bill b
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
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
      JOIN bd.bill b
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
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
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
        AND b.dateOfPayment BETWEEN :from AND :to
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
      WHERE b.status = com.poly.BE_main.utils.BillStatus.HOAN_TAT
        AND b.dateOfPayment BETWEEN :from AND :to
      GROUP BY p.productName, c.name, s.eu
      """)
  List<StockStatisticDTO> getStockStatisticsFiltered(
      @Param("from") LocalDate from,
      @Param("to") LocalDate to);

  List<Bill> findByCustomerId(Integer customerId);

  @Query("""
          SELECT new com.poly.BE_main.dto.InvoiceItemCustomerDTO(
              pd.id,
              p.productName,
              c.name,
              CAST(s.eu AS string),
              bd.quantity,
              bd.price,
              COALESCE((SELECT img.url
                       FROM Image img
                       WHERE img.productDetailId = pd.id
                         AND img.isMain = true), ''),
              b.subTotal,
              b.discountAmount,
              b.shippingFee,
              b.grandTotal
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

  // Truy vấn tùy chỉnh để lấy hóa đơn theo ID
  Optional<Bill> findById(int billId);

  // Truy vấn tùy chỉnh để cập nhật sub_total của hóa đơn
  @Modifying
  @Query("UPDATE Bill b SET b.subTotal = :subTotal WHERE b.id = :billId")
  void updateSubTotal(@Param("billId") int billId, @Param("subTotal") BigDecimal subTotal);

  @Query(value = """
        SELECT
            CONVERT(varchar(10), b.CREATED_DATE, 23) AS createDate,
            COUNT(*) AS billCount,
            SUM(
                ISNULL(b.GRAND_TOTAL,
                      ISNULL(b.SUB_TOTAL,0) - ISNULL(b.DISCOUNT_AMOUNT,0) + ISNULL(b.SHIPPING_FEE,0))
            ) AS totalStatistic
        FROM BILL b
        WHERE b.CREATED_DATE IS NOT NULL
          AND (b.[STATUS] IS NULL OR b.[STATUS] <> 5)  -- 👈 bỏ trạng thái 5
        GROUP BY b.CREATED_DATE
        ORDER BY b.CREATED_DATE DESC;
      """, nativeQuery = true)
  List<Object[]> revenueByDayPaid();

}
