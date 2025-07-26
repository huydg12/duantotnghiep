package com.poly.BE_main.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailDTO {
    private Integer billId;
    private Integer productDetailId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
    private String color;
    private String size;
}
