package com.poly.BE_main.model;


import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PROMOTION_DETAIL")
public class PromotionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PROMOTION_ID")
    private Integer promotionId;

    @Column(name = "PRODUCT_DETAIL_ID")
    private Integer productDetailId;

    @Column(name = "PROMOTION_VALUE")
    private BigDecimal promotionValue;

    @Column(name = "STATUS")
    private Integer status;
}
