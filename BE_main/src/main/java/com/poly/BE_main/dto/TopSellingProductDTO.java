package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductDTO {
    private String productName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}
