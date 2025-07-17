package com.poly.BE_main.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDetailDTO {
    private Integer id;
    private Integer productDetailId;
    private String productName;
    private String size;
    private String color;
    private BigDecimal promotionValue;
    private Integer status;
}
