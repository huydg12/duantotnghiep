package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poly.BE_main.model.CartDetail;

public interface CartDetailRepository extends JpaRepository<CartDetail, Integer> {
    @Query(value = """
            WITH RepresentativeProductDetail AS (
                SELECT
                    PRODUCT_ID,
                    COLOR_ID,
                    MIN(ID) AS REPRESENTATIVE_PD_ID
                FROM PRODUCT_DETAIL
                GROUP BY PRODUCT_ID, COLOR_ID
            )

            SELECT
                CD.ID                  AS cartDetailId,
                C.ID                   AS cartId,
                CU.ID                  AS customerId,
                P.ID                   AS productId,
                P.PRODUCT_NAME         AS productName,
                PD.ID                  AS productDetailId,
                S.EU                   AS size,
                CO.NAME                AS color,
                PD.PRICE               AS price,
                CD.QUANTITY            AS quantity,
                I.URL                  AS image
            FROM CART_DETAIL CD
            JOIN CART C ON CD.CART_ID = C.ID
            JOIN CUSTOMER CU ON C.CUSTOMER_ID = CU.ID
            JOIN PRODUCT_DETAIL PD ON CD.PRODUCT_DETAIL_ID = PD.ID
            JOIN PRODUCT P ON PD.PRODUCT_ID = P.ID
            JOIN SIZE S ON PD.SIZE_ID = S.ID
            JOIN COLOR CO ON PD.COLOR_ID = CO.ID

            -- Lấy product detail đại diện theo màu
            LEFT JOIN RepresentativeProductDetail RPD
                ON PD.PRODUCT_ID = RPD.PRODUCT_ID AND PD.COLOR_ID = RPD.COLOR_ID

            -- Lấy ảnh chính (is_main = 1) từ bản ghi đại diện
            LEFT JOIN IMAGE I
                ON I.PRODUCT_DETAIL_ID = RPD.REPRESENTATIVE_PD_ID AND I.IS_MAIN = 1

            WHERE CU.ID = :customerId
                        """, nativeQuery = true)
    List<Object[]> findAllCartDetailByCustomer(@Param("customerId") Integer customerId);


    @Query(value = """
        SELECT CASE 
                WHEN COUNT(*) > 0 THEN CAST(1 AS BIT) 
                ELSE CAST(0 AS BIT) 
            END 
        FROM CART_DETAIL 
        WHERE CART_ID = :cartId 
        AND PRODUCT_DETAIL_ID = :productDetailId
        """, nativeQuery = true)
    boolean existsByCartIdAndProductDetailId(@Param("cartId") Integer cartId, @Param("productDetailId") Integer productDetailId);
    // ✅ Hàm cập nhật quantity
    @Modifying
    @Query(value = """
        UPDATE CART_DETAIL
        SET QUANTITY = QUANTITY + :quantity
        WHERE CART_ID = :cartId AND PRODUCT_DETAIL_ID = :productDetailId
        """, nativeQuery = true)
    void updateQuantityByCartIdAndProductDetailId(
        @Param("cartId") Integer cartId,
        @Param("productDetailId") Integer productDetailId,
        @Param("quantity") Integer quantity
    );
}