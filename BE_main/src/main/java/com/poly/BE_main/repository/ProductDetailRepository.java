package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poly.BE_main.model.ProductDetail;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Integer> {
    @Query(value = """
                SELECT
                    P.ID AS productId,
                    PD.ID AS productDetailId,
                    P.PRODUCT_NAME AS productName,
                    B.NAME AS brandName,
                    C.NAME AS color,
                    P.DESCRIPTION AS descriptionProduct,
                    S.NAME AS size,
                    PD.PRICE AS price,
                    STRING_AGG(I.URL, ',') AS images
                FROM
                    PRODUCT P
                JOIN
                    PRODUCT_DETAIL PD ON P.ID = PD.PRODUCT_ID
                JOIN
                    BRAND B ON P.BRAND_ID = B.ID
                JOIN
                    COLOR C ON PD.COLOR_ID = C.ID
                JOIN
                    SIZE S ON PD.SIZE_ID = S.ID
                LEFT JOIN
                    IMAGE I ON PD.ID = I.PRODUCT_DETAIL_ID
                WHERE P.ID = :productId
                GROUP BY
                    P.ID, PD.ID, P.PRODUCT_NAME,
                    B.NAME, C.NAME, P.DESCRIPTION, S.NAME, PD.PRICE;
            """, nativeQuery = true)
    List<Object[]> findAllProductDetailDTOByID(@Param("productId") Integer productId);

}