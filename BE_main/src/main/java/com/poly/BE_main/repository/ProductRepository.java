package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poly.BE_main.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query(value = """
                SELECT
                    P.ID AS productId,
                    P.PRODUCT_NAME AS productName,
                    B.NAME AS brandName,
                    PD.PRICE AS price,
                    MAX(CASE WHEN RN = 1 THEN I.URL END) AS image1,
                    MAX(CASE WHEN RN = 2 THEN I.URL END) AS image2
                FROM PRODUCT P
                JOIN BRAND B ON P.BRAND_ID = B.ID

                CROSS APPLY (
                    SELECT TOP 1 *
                    FROM PRODUCT_DETAIL PD
                    WHERE PD.PRODUCT_ID = P.ID
                    ORDER BY PD.ID
                ) PD

                JOIN (
                    SELECT
                        ID, PRODUCT_DETAIL_ID, URL,
                        ROW_NUMBER() OVER (PARTITION BY PRODUCT_DETAIL_ID ORDER BY ID) AS RN
                    FROM IMAGE
                ) I ON I.PRODUCT_DETAIL_ID = PD.ID AND I.RN IN (1, 2)

                WHERE P.STATUS = 1
                GROUP BY P.ID, P.PRODUCT_NAME, B.NAME, PD.PRICE
            """, nativeQuery = true)
    List<Object[]> findAllProductsWithImages();

     @Query(value = """
        SELECT
            P.ID AS productId,
            P.PRODUCT_NAME AS productName,
            B.NAME AS brandName,
            PD.PRICE AS price,
            MAX(CASE WHEN RN = 1 THEN I.URL END) AS image1,
            MAX(CASE WHEN RN = 2 THEN I.URL END) AS image2
        FROM PRODUCT P
        JOIN BRAND B ON P.BRAND_ID = B.ID
        CROSS APPLY (
            SELECT TOP 1 *
            FROM PRODUCT_DETAIL PD
            WHERE PD.PRODUCT_ID = P.ID
            ORDER BY PD.ID
        ) PD
        JOIN (
            SELECT
                ID, PRODUCT_DETAIL_ID, URL,
                ROW_NUMBER() OVER (PARTITION BY PRODUCT_DETAIL_ID ORDER BY ID) AS RN
            FROM IMAGE
        ) I ON I.PRODUCT_DETAIL_ID = PD.ID AND I.RN IN (1, 2)
        WHERE P.STATUS = 1 AND P.PRODUCT_NAME LIKE %:keyword%
        GROUP BY P.ID, P.PRODUCT_NAME, B.NAME, PD.PRICE
        """, nativeQuery = true)
    List<Object[]> searchByKeyword(@Param("keyword") String keyword);
}