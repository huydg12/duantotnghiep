package com.poly.BE_main.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "PROMOTION_DETAIL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer Id;

    @Column(name = "PROMOTION_ID")
    private Integer PromotionId;

    @Column(name = "PROMOTION_DETAIL_ID")
    private Integer PromotionDetailId;

    @Column(name = "PROMOTION_VALUE", precision = 10, scale = 2)
    private BigDecimal PromotionValue;

    @Column(name = "STATUS")
    private Integer Status;

    
}