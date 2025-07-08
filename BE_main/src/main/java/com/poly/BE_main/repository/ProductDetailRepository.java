package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poly.BE_main.model.ProductDetail;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Integer> {
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
                P.ID AS productId,
                PD.ID AS productDetailId,
                P.PRODUCT_NAME AS productName,
                B.NAME AS brandName,
                C.NAME AS color,
                CL.NAME AS collar,
                P.DESCRIPTION AS descriptionProduct,
                S.EU AS size,
                PD.PRICE AS price,
                STRING_AGG(I.URL, ',') AS images
            FROM
                PRODUCT P
            JOIN PRODUCT_DETAIL PD ON P.ID = PD.PRODUCT_ID
            JOIN BRAND B ON P.BRAND_ID = B.ID
            JOIN COLOR C ON PD.COLOR_ID = C.ID
            JOIN SIZE S ON PD.SIZE_ID = S.ID
            LEFT JOIN COLLAR CL ON PD.COLLAR_ID = CL.ID
            LEFT JOIN RepresentativeProductDetail RPD
                ON PD.PRODUCT_ID = RPD.PRODUCT_ID AND PD.COLOR_ID = RPD.COLOR_ID
            LEFT JOIN IMAGE I ON I.PRODUCT_DETAIL_ID = RPD.REPRESENTATIVE_PD_ID
            WHERE P.ID = :productId
            GROUP BY
                P.ID, PD.ID, P.PRODUCT_NAME,
                B.NAME, C.NAME, P.DESCRIPTION, S.EU, CL.NAME, PD.PRICE;
                        """, nativeQuery = true)
    List<Object[]> findAllProductDetailDTOByID(@Param("productId") Integer productId);

}